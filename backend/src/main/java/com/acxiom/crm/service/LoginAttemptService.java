package com.acxiom.crm.service;

import com.acxiom.crm.audit.AuditService;
import com.acxiom.crm.entity.AppUser;
import com.acxiom.crm.entity.AuditAction;
import com.acxiom.crm.repository.AppUserRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Failed-login tracking and temporary lockout (p.8 §6.3).
 * <p>
 * This is a separate bean, not a private method on {@link AuthService}, for a
 * concrete reason: the attempt counter must be committed even though the login
 * request itself ends in an authentication failure. Spring's transaction proxy
 * only applies to calls that cross a bean boundary, so a private method inside
 * AuthService would join the caller's transaction and be rolled back along with
 * the failed login - leaving the counter permanently at zero and making the
 * lockout rule silently inert. {@code REQUIRES_NEW} here guarantees the write.
 */
@Service
public class LoginAttemptService {

    private final AppUserRepository userRepository;
    private final AuditService auditService;
    private final int maxFailedAttempts;
    private final int lockoutDurationMinutes;

    public LoginAttemptService(AppUserRepository userRepository,
                               AuditService auditService,
                               @Value("${acxiom.security.lockout.max-failed-attempts}") int maxFailedAttempts,
                               @Value("${acxiom.security.lockout.duration-minutes}") int lockoutDurationMinutes) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }

    /**
     * Counts one failed attempt and locks the account once the configured
     * threshold is reached.
     *
     * @param user the account, or null when the email is unknown - an unknown
     *             email still gets an audit entry but has no counter to update
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(AppUser user, String attemptedEmail) {
        if (user == null) {
            // Do not disclose whether the account exists, but do record the attempt.
            auditService.record(null, "Authentication", AuditAction.LOGIN_FAILURE, "User",
                    attemptedEmail, "FAILURE", "Login failed: unknown email.");
            return;
        }

        if (user.isLockedOut()) {
            // The account is already locked, so the attempt is refused and recorded
            // but must not advance the counter - otherwise every attempt while
            // locked would push the lock further into the future and an attacker
            // could hold an account locked indefinitely.
            auditService.record(user, "Authentication", AuditAction.LOGIN_FAILURE, "User",
                    String.valueOf(user.getId()), "FAILURE",
                    "Login refused: account is locked until " + user.getLockoutEnd() + ".");
            return;
        }

        int attempts = user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(attempts);

        if (attempts >= maxFailedAttempts) {
            user.setLockoutEnd(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
            // Reset the counter so the next window starts clean after the lock expires.
            user.setFailedLoginCount(0);
            userRepository.save(user);
            auditService.record(user, "Authentication", AuditAction.LOCKOUT, "User",
                    String.valueOf(user.getId()), "SUCCESS",
                    "Account locked for " + lockoutDurationMinutes
                            + " minutes after " + attempts + " failed login attempts.");
            return;
        }

        userRepository.save(user);
        auditService.record(user, "Authentication", AuditAction.LOGIN_FAILURE, "User",
                String.valueOf(user.getId()), "FAILURE",
                "Login failed. Attempt " + attempts + " of " + maxFailedAttempts + ".");
    }

    /** Clears the counter and any expired lock after a successful login. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AppUser recordSuccess(AppUser user) {
        if (user.getFailedLoginCount() != 0 || user.getLockoutEnd() != null) {
            user.setFailedLoginCount(0);
            user.setLockoutEnd(null);
            user = userRepository.save(user);
        }
        auditService.record(user, "Authentication", AuditAction.LOGIN_SUCCESS, "User",
                String.valueOf(user.getId()), "SUCCESS", "Login successful.");
        return user;
    }
}
