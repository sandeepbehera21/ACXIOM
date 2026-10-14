package com.acxiom.crm.repository;

import com.acxiom.crm.entity.FollowUp;
import com.acxiom.crm.entity.FollowUpStatus;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FollowUpRepository
        extends JpaRepository<FollowUp, Long>, JpaSpecificationExecutor<FollowUp> {

    long countByAssignedToId(Long assignedToId);

    long countByStatus(FollowUpStatus status);

    static Specification<FollowUp> filter(
            LocalDate dateFrom,
            LocalDate dateTo,
            FollowUpStatus status,
            Long assignedToId,
            Long customerId,
            Long leadId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("followUpDate"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("followUpDate"), dateTo));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (assignedToId != null) {
                predicates.add(cb.equal(root.get("assignedTo").get("id"), assignedToId));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            }
            if (leadId != null) {
                predicates.add(cb.equal(root.get("lead").get("id"), leadId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
