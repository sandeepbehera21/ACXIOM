package com.acxiom.crm.repository;

import com.acxiom.crm.entity.Opportunity;
import com.acxiom.crm.entity.OpportunityStage;
import com.acxiom.crm.entity.OpportunityStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface OpportunityRepository
        extends JpaRepository<Opportunity, Long>, JpaSpecificationExecutor<Opportunity> {

    long countByOwnerId(Long ownerId);

    /** Used to block deleting a customer that still has opportunities. */
    long countByCustomerId(Long customerId);

    long countByStatus(OpportunityStatus status);

    List<Opportunity> findByStatus(OpportunityStatus status);

    /**
     * Search and filter required by p.17 §17.13:
     * "Opportunities - Opportunity Name, Customer, Stage, Status".
     */
    static Specification<Opportunity> filter(String search, OpportunityStage stage,
                                             OpportunityStatus status, Long ownerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("customer").get("name")), like)));
            }
            if (stage != null) {
                predicates.add(cb.equal(root.get("stage"), stage));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (ownerId != null) {
                predicates.add(cb.equal(root.get("owner").get("id"), ownerId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
