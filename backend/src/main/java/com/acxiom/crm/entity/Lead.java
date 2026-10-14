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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Sales lead.
 * <p>
 * Fields are the union of both specification listings:
 * <ul>
 *   <li>p.10 §9: LeadId, Name, Email, Phone, Source, Status, Priority, OwnerId, CreatedDate</li>
 *   <li>p.15 §17.3: LeadId, LeadCode, LeadName, Email, Phone, CompanyName, Source,
 *       Status, ExpectedValue, CreatedDate, AssignedTo</li>
 * </ul>
 * Conversion tracking columns exist so that §4.5's "Support conversion of
 * qualified leads into customer/opportunity records" is auditable.
 */
@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lead_id")
    private Long id;

    /** Human-readable business key, e.g. LED-000001 (p.15 §17.3). */
    @Column(name = "lead_code", nullable = false, unique = true, length = 20)
    private String leadCode;

    @Column(name = "lead_name", nullable = false, length = 150)
    private String name;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "company_name", length = 150)
    private String companyName;

    /** Not enumerated by the specification; stored as free text. */
    @Column(name = "source", length = 50)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LeadStatus status = LeadStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 10)
    private LeadPriority priority = LeadPriority.MEDIUM;

    /** ExpectedValue from §17.3; "must be within the configured numeric range" (§17.6). */
    @Column(name = "expected_value", precision = 15, scale = 2)
    private BigDecimal expectedValue;

    @Column(name = "notes", length = 1000)
    private String notes;

    /** AssignedTo / OwnerId. Drives the ownership restriction. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private AppUser assignedTo;

    /** Set when the lead is converted (specification §4.5, §8 workflow step 5). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_customer_id")
    private Customer convertedCustomer;

    @Column(name = "converted_date")
    private LocalDateTime convertedDate;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    public boolean isConverted() {
        return status == LeadStatus.CONVERTED;
    }
}
