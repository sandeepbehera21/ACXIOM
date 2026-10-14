package com.acxiom.crm.dto;

import com.acxiom.crm.entity.OpportunityStage;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Optional opportunity details supplied while converting a lead.
 * <p>
 * The specification's lead-to-customer workflow (p.9 §8) step 5 is "Convert to
 * Customer/Opportunity", so both records may be produced in one transaction.
 */
public record LeadConversionRequest(
        /** When false only the customer is created. */
        boolean createOpportunity,

        String opportunityName,

        @DecimalMin(value = "0.0", inclusive = false,
                message = "Opportunity Amount must be greater than 0.")
        @Digits(integer = 13, fraction = 2)
        BigDecimal amount,

        @Min(value = 0, message = "Probability must be between 0 and 100.")
        @Max(value = 100, message = "Probability must be between 0 and 100.")
        Integer probability,

        LocalDate expectedCloseDate,

        OpportunityStage stage,

        String source) {
}
