package com.acxiom.crm.service;

import com.acxiom.crm.dto.AuditLogResponse;
import com.acxiom.crm.entity.AuditAction;
import com.acxiom.crm.repository.AuditLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read access to the audit trail (p.5-6 §4.9, p.18 §17.16).
 * <p>
 * This exists as a service rather than querying from the controller for a
 * concrete reason: an audit entry carries a lazy reference to the user who acted,
 * and the response includes that user's name. Mapping the entity to the DTO after
 * the repository call returns - with the transaction already closed - would throw
 * a lazy-initialisation error. Holding the mapping inside a read transaction is
 * what makes it work.
 * <p>
 * There is no create, update or delete method here. Entries may only be appended
 * by {@code AuditService}, and the repository itself exposes no way to change one.
 */
@Service
public class AuditLogService {

    /** Upper bound on page size, so one request cannot ask for the whole table. */
    private static final int MAX_PAGE_SIZE = 200;

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(Long userId, String module, AuditAction action,
                                         LocalDateTime from, LocalDateTime to,
                                         int page, int size) {
        // Clamp rather than reject: a hostile or careless page size must not be
        // able to pull the entire audit table into memory.
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        return auditLogRepository
                .findAll(AuditLogRepository.filter(userId, module, action, from, to),
                        PageRequest.of(safePage, safeSize,
                                Sort.by(Sort.Direction.DESC, "createdDate")))
                .map(AuditLogResponse::from);
    }

    /** The module names used by the application, for the filter dropdown. */
    public List<String> modules() {
        return List.of("Authentication", "User Management", "Customer", "Lead", "Opportunity");
    }
}
