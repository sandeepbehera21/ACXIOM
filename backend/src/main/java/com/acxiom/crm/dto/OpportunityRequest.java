package com.acxiom.crm.dto;

import com.acxiom.crm.entity.OpportunityStage;
import com.acxiom.crm.entity.OpportunityStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Create/update payload for an opportunity.
 * <p>
 * The annotation messages match the exact wording required by p.8 §5.4:
 * "Opportunity Amount must be greater than 0." and
 * "Probability must be between 0 and 100."
 * <p>
 * The Expected Close Date rule depends on whether the opportunity is active, so
 * it is enforced in the service rather than by a {@code @FutureOrPresent}
 * annotation that would also reject historical Won/Lost records.
 */
public record OpportunityRequest(
        @NotBlank(message = "Opportunity name is required.")
        @Size(max = 150, message = "Opportunity name must not exceed 150 characters.")
        String name,

        @NotNull(message = "Customer is required.")
        Long customerId,

        /** Optional link back to the originating lead. */
        Long leadId,

        @NotNull(message = "Amount is required.")
        @DecimalMin(value = "0.0", inclusive = false,
                message = "Opportunity Amount must be greater than 0.")
        @Digits(integer = 13, fraction = 2, message = "Amount must have at most 2 decimal places.")
        BigDecimal amount,

        @NotNull(message = "Stage is required.")
        OpportunityStage stage,

        OpportunityStatus status,

        @NotNull(message = "Probability is required.")
        @Min(value = 0, message = "Probability must be between 0 and 100.")
        @Max(value = 100, message = "Probability must be between 0 and 100.")
        Integer probability,

        LocalDate expectedCloseDate,

        @Size(max = 50, message = "Source must not exceed 50 characters.")
        String source,

        @Size(max = 1000, message = "Notes must not exceed 1000 characters.")
        String notes,

        /** Owner. Null keeps the existing owner. */
        Long ownerId) {
}
