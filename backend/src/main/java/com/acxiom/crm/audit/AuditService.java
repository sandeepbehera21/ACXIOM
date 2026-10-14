package com.acxiom.crm.audit;

import com.acxiom.crm.entity.AppUser;
import com.acxiom.crm.entity.AuditAction;
import com.acxiom.crm.entity.AuditLog;
import com.acxiom.crm.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Writes audit entries.
 * <p>
 * Required by p.5-6 §4.9 and p.18 §17.16: login success/failure, logout, create,
 * update, delete, role changes, password/security events and important status
 * changes must be recorded with user, timestamp, action, module, record
 * identifier, result and relevant metadata.
 * <p>
 * Two rules from the specification are enforced here rather than left to
 * convention:
 * <ul>
 *   <li>p.8 §6.2 - "Do not log passwords, password hashes, reset tokens or
 *       authentication secrets". No method on this class accepts a password.</li>
 *   <li>p.5-6 §4.9 - audit records are append-only. Nothing here updates or
 *       deletes a row.</li>
 * </ul>
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    /** Long values are truncated so a large record cannot bloat the audit table. */
    private static final int MAX_VALUE_LENGTH = 4000;

    /** The details column is narrower than the old/new value columns. */
    private static final int MAX_DETAILS_LENGTH = 500;

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Record an event.
     * <p>
     * Deliberately uses the <b>default</b> transaction propagation, so the audit
     * row joins whatever transaction the caller already has. Two reasons:
     * <ul>
     *   <li>A business audit row references the very record being written, and
     *       that record is not visible outside the caller's transaction until it
     *       commits. Auditing in a separate transaction would break the foreign
     *       key for any newly created row.</li>
     *   <li>The audit of a change should commit with the change. If the operation
     *       rolls back, so does its audit - we must not claim a change happened
     *       when it did not.</li>
     * </ul>
     * Authentication events are the exception, and they need no special handling
     * here: {@code LoginAttemptService} already runs its own
     * {@code REQUIRES_NEW} transaction, so a failed login is recorded even though
     * the request itself fails. That only works because login is not itself
     * transactional, so there is no uncommitted parent row to reference.
     */
    public void record(AppUser user, String module, AuditAction action, String entityName,
                       String recordId, String result, String details,
                       String oldValue, String newValue) {
        try {
            AuditLog entry = new AuditLog(user, module, action, entityName, recordId,
                    result, truncate(details, MAX_DETAILS_LENGTH), currentIpAddress());
            entry.setOldValue(truncate(oldValue, MAX_VALUE_LENGTH));
            entry.setNewValue(truncate(newValue, MAX_VALUE_LENGTH));
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            // Auditing must never break the business operation, but it must be
            // visible in the server log if it fails.
            log.error("Failed to write audit entry: action={} module={} recordId={}",
                    action, module, recordId, ex);
        }
    }

    /** Convenience for the common case with no old/new value comparison. */
    public void record(AppUser user, String module, AuditAction action, String entityName,
                       String recordId, String result, String details) {
        record(user, module, action, entityName, recordId, result, details, null, null);
    }

    /**
     * Client address, required by p.18 §17.16 ("IpAddress where available").
     * X-Forwarded-For is honoured so the value stays correct behind a proxy.
     */
    private String currentIpAddress() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception ex) {
            return null;
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
    }
}
