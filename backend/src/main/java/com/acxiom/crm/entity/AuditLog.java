package com.acxiom.crm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Audit record.
 * <p>
 * Fields are the union of both specification listings:
 * <ul>
 *   <li>p.10 §9: AuditLogId, UserId, Module, Action, RecordId, Timestamp, Result, Details</li>
 *   <li>p.15 §17.3: AuditLogId, UserId, Action, EntityName, RecordId, OldValue,
 *       NewValue, CreatedDate, IpAddress</li>
 * </ul>
 * The specification requires audit rows to be append-oriented and protected
 * from unauthorised modification or deletion (p.5-6 §4.9), so this entity is
 * only ever inserted, never updated or deleted; there is no DELETE endpoint for
 * it and the API layer exposes read-only access.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_log_id")
    private Long id;

    /** Nullable: a failed login for an unknown username has no user row. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    /** Module from §9, e.g. "Authentication", "Customer", "Opportunity". */
    @Column(name = "module", length = 50)
    private String module;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private AuditAction action;

    /** EntityName from §17.3. */
    @Column(name = "entity_name", length = 50)
    private String entityName;

    @Column(name = "record_id", length = 50)
    private String recordId;

    @Lob
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Lob
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /** Result from §9, e.g. "SUCCESS" or "FAILURE". */
    @Column(name = "result", length = 20)
    private String result;

    @Column(name = "details", length = 500)
    private String details;

    /** Timestamp from §9 / CreatedDate from §17.3. */
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Never include credentials in an audit row (p.8 §6.2: "Do not log
     * passwords, password hashes, reset tokens or authentication secrets").
     * This constructor deliberately has no password parameter.
     */
    public AuditLog(AppUser user, String module, AuditAction action, String entityName,
                    String recordId, String result, String details, String ipAddress) {
        this.user = user;
        this.module = module;
        this.action = action;
        this.entityName = entityName;
        this.recordId = recordId;
        this.result = result;
        this.details = details;
        this.ipAddress = ipAddress;
        this.createdDate = LocalDateTime.now();
    }
}
