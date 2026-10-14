package com.acxiom.crm.controller;

import com.acxiom.crm.dto.AuditLogResponse;
import com.acxiom.crm.entity.AuditAction;
import com.acxiom.crm.service.AuditLogService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The audit trail, read-only (p.5-6 §4.9, p.18 §17.16).
 * <p>
 * Restricted to the Admin (p.9 §7.1). There is no endpoint that modifies an audit
 * entry, which is how the append-only requirement is upheld: the capability does
 * not exist rather than being merely discouraged.
 * <p>
 * The guard is declared here as a method-level rule as well as in
 * {@code SecurityConfig}, so the restriction holds even if the route is remapped.
 */
@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public Page<AuditLogResponse> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        return auditLogService.search(userId, module, action, from, to, page, size);
    }

    @GetMapping("/modules")
    public List<String> modules() {
        return auditLogService.modules();
    }
}
