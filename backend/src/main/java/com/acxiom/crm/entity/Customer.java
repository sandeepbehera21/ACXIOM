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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Customer master data.
 * <p>
 * Fields are the union of both specification listings, because no mandatory
 * field may be silently dropped:
 * <ul>
 *   <li>p.10 §9: Name, Email, Phone, Address, Status, OwnerId, CreatedDate, ModifiedDate</li>
 *   <li>p.15 §17.3: CustomerCode, CustomerName, Email, Phone, CompanyName,
 *       Address, City, State, Status, CreatedDate, CreatedBy</li>
 *   <li>p.4 §4.4 additionally requires notes and "assigned sales executive"</li>
 * </ul>
 * Email and phone are unique (p.15 §17.5: "Email uniqueness must be enforced",
 * "Phone uniqueness must be enforced", "Duplicate customer creation must be
 * prevented").
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;

    /** Human-readable business key, e.g. CUS-000001 (p.15 §17.3). */
    @Column(name = "customer_code", nullable = false, unique = true, length = 20)
    private String customerCode;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone", nullable = false, unique = true, length = 15)
    private String phone;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CustomerStatus status = CustomerStatus.PROSPECT;

    @Column(name = "notes", length = 1000)
    private String notes;

    /** "assigned sales executive" / OwnerId. Drives the ownership restriction. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    /** CreatedBy from §17.3. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AppUser createdBy;
}
