package com.acxiom.crm.repository;

import com.acxiom.crm.entity.Customer;
import com.acxiom.crm.entity.CustomerStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface CustomerRepository
        extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByCustomerCode(String customerCode);

    Optional<Customer> findFirstByOrderByIdDesc();

    /** Duplicate prevention: §17.5 "Email uniqueness must be enforced". */
    boolean existsByEmailIgnoreCase(String email);

    /** Used on update, where the record's own email and phone must not count as a clash. */
    Optional<Customer> findByEmailIgnoreCase(String email);

    Optional<Customer> findByPhone(String phone);

    /** Duplicate prevention: §17.5 "Phone uniqueness must be enforced". */
    boolean existsByPhone(String phone);

    long countByOwnerId(Long ownerId);

    /**
     * Search and filter required by p.17 §17.13: "Customers - Customer Name, Email,
     * Phone, Company" plus a status filter. Built with the Criteria API so every
     * value is bound as a parameter (no string concatenation into SQL - §17.9).
     * A null argument means "no filter on this field".
     */
    static Specification<Customer> filter(String search, CustomerStatus status, Long ownerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(root.get("phone"), like),
                        cb.like(cb.lower(cb.coalesce(root.get("companyName"), "")), like)));
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
