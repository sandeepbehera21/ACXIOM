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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Sales opportunity.
 * <p>
 * Fields are the union of both specification listings:
 * <ul>
 *   <li>p.10 §9: OpportunityId, CustomerId, Name, Stage, Amount, Probability,
 *       ExpectedCloseDate, OwnerId, Notes</li>
 *   <li>p.15 §17.3: OpportunityId, OpportunityName, CustomerId, LeadId, Amount,
 *       Stage, Probability, ExpectedCloseDate, Status, CreatedDate, AssignedTo</li>
 * </ul>
 * Business rules enforced in {@code OpportunityService}:
 * Amount &gt; 0 for an active opportunity, Probability 0-100 inclusive, and
 * ExpectedCloseDate not in the past for an active opportunity
 * (p.5 §4.8, p.7 §5.3, p.16 §17.7).
 */
@Entity
@Table(name = "opportunities")
@Getter
@Setter
@NoArgsConstructor
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opportunity_id")
    private Long id;

    @Column(name = "opportunity_name", nullable = false, length = 150)
    private String name;

    /** Required for an opportunity to exist; §17.3 lists CustomerId as mandatory. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** Optional: an opportunity may originate from a converted lead. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    /** "Must be greater than 0" for an active opportunity (§4.8, §5.3, §17.7). */
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false, length = 20)
    private OpportunityStage stage = OpportunityStage.QUALIFICATION;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OpportunityStatus status = OpportunityStatus.OPEN;

    /** "Must be between 0 and 100 inclusive" (§4.8, §5.3, §17.7). */
    @Column(name = "probability", nullable = false)
    private Integer probability = 0;

    /** "Cannot be in the past for an active opportunity" (§4.8, §5.3, §17.7). */
    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "notes", length = 1000)
    private String notes;

    /** AssignedTo / OwnerId. Drives the ownership restriction. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    /**
     * Weighted pipeline value, required by p.5 §4.8:
     * "Calculate/derive weighted pipeline where required using Amount x Probability / 100".
     */
    public BigDecimal getWeightedAmount() {
        if (amount == null || probability == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(BigDecimal.valueOf(probability))
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }
}
