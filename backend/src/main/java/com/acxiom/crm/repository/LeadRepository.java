package com.acxiom.crm.repository;

import com.acxiom.crm.entity.Lead;
import com.acxiom.crm.entity.LeadStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface LeadRepository extends JpaRepository<Lead, Long>, JpaSpecificationExecutor<Lead> {

    Optional<Lead> findFirstByOrderByIdDesc();

    long countByAssignedToId(Long assignedToId);

    long countByStatusNotIn(List<LeadStatus> statuses);

    /** Used to block deleting a customer that was produced by converting a lead. */
    long countByConvertedCustomerId(Long customerId);

    /**
     * Search and filter required by p.17 §17.13:
     * "Leads - Lead Name, Company, Status, Assigned User".
     */
    static Specification<Lead> filter(String search, LeadStatus status, Long assignedToId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("companyName"), "")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("email"), "")), like),
                        cb.like(cb.coalesce(root.get("phone"), ""), like)));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (assignedToId != null) {
                predicates.add(cb.equal(root.get("assignedTo").get("id"), assignedToId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
