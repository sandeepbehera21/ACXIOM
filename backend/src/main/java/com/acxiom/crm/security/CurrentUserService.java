package com.acxiom.crm.security;

import com.acxiom.crm.entity.AppUser;
import com.acxiom.crm.exception.ResourceNotFoundException;
import com.acxiom.crm.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves the authenticated user and answers the two questions the service
 * layer needs for authorization:
 * <ol>
 *   <li>who is acting (for ownership and for the audit trail), and</li>
 *   <li>may this user see or change this record?</li>
 * </ol>
 * <p>
 * The specification insists authorization is enforced on the server, not just by
 * hiding navigation links (p.9 §7: "Authorization should be enforced on the
 * server, not only by hiding navigation links"), so every service calls
 * {@link #requireAccessTo} before touching a record.
 */
@Service
public class CurrentUserService {

    private final AppUserRepository userRepository;

    public CurrentUserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** The authenticated user as a managed entity. */
    @Transactional(readOnly = true)
    public AppUser requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new ResourceNotFoundException("No authenticated user in context.");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user no longer exists."));
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isManager() {
        return hasRole("MANAGER");
    }

    /**
     * Sales Executives are restricted to their own records; Admin and Manager
     * see everything (p.9 §7.1: Admin "Full", Manager "Team", Sales Executive
     * "Own/Assigned").
     */
    public boolean canSeeAllRecords() {
        return isAdmin() || isManager();
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Enforces the ownership restriction. Throws the same "not found" error an
     * genuinely missing record produces, so a Sales Executive cannot probe for
     * the existence of other people's records.
     */
    public void requireAccessTo(Long recordOwnerId, String entityName, Long recordId) {
        if (canSeeAllRecords()) {
            return;
        }
        AppUser current = requireCurrentUser();
        if (recordOwnerId == null || !recordOwnerId.equals(current.getId())) {
            throw new ResourceNotFoundException(
                    entityName + " not found with id " + recordId);
        }
    }
}
