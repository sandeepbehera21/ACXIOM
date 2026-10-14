package com.acxiom.crm.repository;

import com.acxiom.crm.entity.AuditAction;
import com.acxiom.crm.entity.AuditLog;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

/**
 * Read-and-append only. There is deliberately no method here that updates or
 * deletes an audit row: p.5-6 §4.9 requires audit records to be
 * "append-oriented and protected from unauthorized modification/deletion".
 */
public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    /**
     * Filtering required by p.6 §4.9: "Provide filtering by user, module, action
     * and date range."
     */
    static Specification<AuditLog> filter(Long userId, String module, AuditAction action,
                                          LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (module != null && !module.isBlank()) {
                predicates.add(cb.equal(root.get("module"), module));
            }
            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdDate"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
