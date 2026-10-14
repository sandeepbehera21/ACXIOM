package com.acxiom.crm.repository;

import com.acxiom.crm.entity.Activity;
import com.acxiom.crm.entity.ActivityStatus;
import com.acxiom.crm.entity.ActivityType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ActivityRepository
        extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {

    long countByAssignedToId(Long assignedToId);

    static Specification<Activity> filter(
            ActivityType activityType,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            ActivityStatus status,
            Long assignedToId,
            Long customerId,
            Long leadId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (activityType != null) {
                predicates.add(cb.equal(root.get("activityType"), activityType));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("activityDate"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("activityDate"), dateTo));
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
