package com.acxiom.crm.service;

import com.acxiom.crm.audit.AuditService;
import com.acxiom.crm.dto.CreateUserRequest;
import com.acxiom.crm.dto.UserResponse;
import com.acxiom.crm.entity.AppUser;
import com.acxiom.crm.entity.AuditAction;
import com.acxiom.crm.entity.Role;
import com.acxiom.crm.entity.RoleName;
import com.acxiom.crm.exception.BusinessRuleException;
import com.acxiom.crm.exception.DuplicateResourceException;
import com.acxiom.crm.exception.ResourceNotFoundException;
import com.acxiom.crm.repository.AppUserRepository;
import com.acxiom.crm.repository.RoleRepository;
import com.acxiom.crm.security.CurrentUserService;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User administration (p.7 §4.7, p.9 §7.1).
 */
@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository,
                       RoleRepository roleRepository,
                       CurrentUserService currentUserService,
                       AuditService auditService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll(String search) {
        requireAdminOrManager("view the user list");
        List<AppUser> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByNameAsc(
                    search.trim(), search.trim());
        } else {
            users = userRepository.findAllByOrderByNameAsc();
        }
        return users.stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        requireAdminOrManager("view a user");
        return UserResponse.from(requireUser(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        AppUser acting = requireAdmin("create a user");

        if (userRepository.existsByEmailIgnoreCase(request.email().trim())) {
            throw new DuplicateResourceException("email", "A user with this email already exists.");
        }

        Role role = roleRepository.findByRoleName(request.role())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.role()));

        AppUser user = new AppUser();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setActive(true);

        AppUser saved = userRepository.save(user);

        auditService.record(acting, "User Management", AuditAction.CREATE, "User",
                String.valueOf(saved.getId()), "SUCCESS",
                "Created user " + saved.getEmail() + " with role " + saved.getRole().getRoleName() + ".");

        return UserResponse.from(saved);
    }

    /** Role change (p.7 §4.7, p.9 §7.1) - the most security-sensitive operation here. */
    @Transactional
    public UserResponse changeRole(Long id, RoleName roleName) {
        AppUser acting = requireAdmin("change a user role");
        AppUser target = requireUser(id);

        if (target.getId().equals(acting.getId())) {
            throw new BusinessRuleException("role",
                    "You cannot change your own role. Ask another administrator to do it.");
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        RoleName previous = target.getRole().getRoleName();
        if (previous == roleName) {
            throw new BusinessRuleException("role",
                    "This user already has the " + roleName + " role.");
        }

        target.setRole(role);
        AppUser saved = userRepository.save(target);

        auditService.record(acting, "User Management", AuditAction.ROLE_CHANGE, "User",
                String.valueOf(id), "SUCCESS",
                "Role changed for " + saved.getEmail() + " from " + previous + " to " + roleName + ".",
                "role=" + previous, "role=" + roleName);
        return UserResponse.from(saved);
    }

    /** Activate or deactivate (p.7 §4.7). Deactivation blocks login immediately. */
    @Transactional
    public UserResponse updateStatus(Long id, boolean active) {
        AppUser acting = requireAdmin("change a user's status");
        AppUser target = requireUser(id);

        if (target.getId().equals(acting.getId()) && !active) {
            throw new BusinessRuleException("active",
                    "You cannot deactivate your own account.");
        }
        if (target.isActive() == active) {
            throw new BusinessRuleException("active",
                    active ? "This user is already active." : "This user is already inactive.");
        }

        target.setActive(active);
        AppUser saved = userRepository.save(target);

        auditService.record(acting, "User Management", AuditAction.UPDATE, "User",
                String.valueOf(id), "SUCCESS",
                "Account " + (active ? "activated" : "deactivated") + " for " + saved.getEmail() + ".",
                "active=" + !active, "active=" + active);
        return UserResponse.from(saved);
    }

    /** Clears a temporary lockout before it expires (p.8 §6.3). */
    @Transactional
    public UserResponse unlock(Long id) {
        AppUser acting = requireAdmin("unlock a user account");
        AppUser target = requireUser(id);

        if (!target.isLockedOut() && target.getFailedLoginCount() == 0) {
            throw new BusinessRuleException("id", "This account is not locked.");
        }

        target.setLockoutEnd(null);
        target.setFailedLoginCount(0);
        AppUser saved = userRepository.save(target);

        auditService.record(acting, "User Management", AuditAction.UNLOCK, "User",
                String.valueOf(id), "SUCCESS",
                "Account unlocked for " + saved.getEmail() + " by an administrator.");
        return UserResponse.from(saved);
    }

    // ---------------------------------------------------------------- helpers

    private AppUser requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    /**
     * Second line of defence behind the URL rule. A non-Admin reaching these
     * operations is denied here even if a route were ever mis-configured.
     */
    private AppUser requireAdmin(String action) {
        AppUser acting = currentUserService.requireCurrentUser();
        if (!currentUserService.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only an Administrator may " + action + ".");
        }
        return acting;
    }

    private AppUser requireAdminOrManager(String action) {
        AppUser acting = currentUserService.requireCurrentUser();
        if (!currentUserService.isAdmin() && !currentUserService.isManager()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only an Administrator or Manager may " + action + ".");
        }
        return acting;
    }
}
