package com.acxiom.crm.dto;

import com.acxiom.crm.entity.AuditLog;
import java.time.LocalDateTime;

/**
 * Audit row as returned to the client.
 * <p>
 * Carries exactly the fields p.18 §17.16 requires to be captured:
 * "UserId, Action, EntityName, RecordId, OldValue, NewValue, CreatedDate and
 * IpAddress". Read-only: the specification requires audit records to be
 * protected from modification and deletion (p.5-6 §4.9).
 */
public record AuditLogResponse(
        Long id,
        Long userId,
        String userName,
        String module,
        String action,
        String entityName,
        String recordId,
        String oldValue,
        String newValue,
        String result,
        String details,
        LocalDateTime createdDate,
        String ipAddress) {

    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUser() == null ? null : log.getUser().getId(),
                log.getUser() == null ? null : log.getUser().getName(),
                log.getModule(),
                log.getAction() == null ? null : log.getAction().name(),
                log.getEntityName(),
                log.getRecordId(),
                log.getOldValue(),
                log.getNewValue(),
                log.getResult(),
                log.getDetails(),
                log.getCreatedDate(),
                log.getIpAddress());
    }
}
