package com.acxiom.crm.service;

import com.acxiom.crm.audit.AuditService;
import com.acxiom.crm.dto.LoginRequest;
import com.acxiom.crm.dto.RegisterRequest;
import com.acxiom.crm.dto.UserResponse;
import com.acxiom.crm.entity.AppUser;
import com.acxiom.crm.entity.AuditAction;
import com.acxiom.crm.entity.RoleName;
import com.acxiom.crm.exception.BusinessRuleException;
import com.acxiom.crm.exception.DuplicateResourceException;
import com.acxiom.crm.repository.AppUserRepository;
import com.acxiom.crm.repository.RoleRepository;
import com.acxiom.crm.security.AppUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Login, logout and self-registration.
 * <p>
 * Replaces ASP.NET Core Identity's SignInManager / UserManager. The outcomes the
 * specification requires are unchanged (p.11 §10, p.16 §17.9): verified
 * credentials, a secure session, hashed passwords, an audit trail, and a
 * temporary lockout after repeated failures.
 */
@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final AuditService auditService;

    public AuthService(AppUserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       LoginAttemptService loginAttemptService,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.loginAttemptService = loginAttemptService;
        this.auditService = auditService;
    }

    /**
     * Verifies credentials and establishes the session.
     * <p>
     * Not annotated {@code @Transactional}: the failure path must write the
     * attempt counter in its own committed transaction (see
     * {@link LoginAttemptService}), and the success path does the same.
     */
    public UserResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email()).orElse(null);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.email(), request.password()));
        } catch (AuthenticationException ex) {
            // Counts the attempt and writes LOGIN_FAILURE / LOCKOUT, then rethrows so
            // the API returns 401 without revealing which part was wrong.
            loginAttemptService.recordFailure(user, request.email());
            throw ex;
        }

        AppUser authenticated = loginAttemptService.recordSuccess(
                userRepository.findById(((AppUserPrincipal) authentication.getPrincipal()).getId())
                        .orElseThrow());

        establishSession(authentication, httpRequest);
        return UserResponse.from(authenticated);
    }

    /**
     * Stores the authentication in the server-side session.
     * <p>
     * Spring Security's built-in login filters are disabled because this API
     * returns JSON, so the session is established explicitly here - including the
     * session-id change that protects against session fixation (p.13 §14).
     */
    private void establishSession(Authentication authentication, HttpServletRequest httpRequest) {
        HttpSession existing = httpRequest.getSession(false);
        if (existing != null) {
            // New session id, same session data: an attacker who planted a session
            // id before login cannot use it afterwards.
            httpRequest.changeSessionId();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        httpRequest.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }

    /** Ends the session and records the logout (p.11 §10, p.4 §4.9). */
    public void logout(HttpServletRequest httpRequest) {
        AppUser current = currentUserOrNull();

        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        if (current != null) {
            auditService.record(current, "Authentication", AuditAction.LOGOUT, "User",
                    String.valueOf(current.getId()), "SUCCESS", "Logout successful.");
        }
    }

    /**
     * Self-registration.
     * <p>
     * The specification requires a public registration page (p.12 §12, p.17
     * §17.19 step 2) yet also requires that only an Admin creates users and
     * assigns roles (p.7 §4.7, p.9 §7.1). {@link RegisterRequest} therefore
     * carries no role field at all: a self-registered account is always a Sales
     * Executive, and any other account is created by an Admin. This closes the
     * privilege-escalation hole that would exist if the role came from the
     * request body.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessRuleException("confirmPassword", "Passwords do not match.");
        }

        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("email",
                    "An account with this email already exists.");
        }

        AppUser user = new AppUser();
        user.setName(request.name().trim());
        user.setEmail(email);
        // BCrypt, never plain text (p.15 §17.4, p.16 §17.9).
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(roleRepository.findByRoleName(RoleName.SALES_EXECUTIVE)
                .orElseThrow(() -> new IllegalStateException("SALES_EXECUTIVE role is missing.")));
        user.setActive(true);

        AppUser saved = userRepository.save(user);

        auditService.record(saved, "User Management", AuditAction.CREATE, "User",
                String.valueOf(saved.getId()), "SUCCESS",
                "Self-registered account created with role SALES_EXECUTIVE.");

        return UserResponse.from(saved);
    }

    /** The authenticated user, or null when the caller is anonymous. */
    private AppUser currentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AppUserPrincipal principal) {
            return userRepository.findById(principal.getId()).orElse(null);
        }
        return null;
    }
}
