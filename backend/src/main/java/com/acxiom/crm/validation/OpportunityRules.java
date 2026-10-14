package com.acxiom.crm.validation;

import com.acxiom.crm.entity.OpportunityStatus;
import com.acxiom.crm.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The opportunity business rules from p.4-5 §4.8, p.6 §5.3 and p.16 §17.7.
 * <p>
 * They live in one place so that the same rules apply whether an opportunity is
 * created directly, edited, or produced by converting a lead - the specification
 * lists these rules three separate times and they must not diverge.
 * <p>
 * These are deliberately checks that Bean Validation annotations cannot express:
 * "greater than 0" and "between 0 and 100" are annotations, but "cannot be in the
 * past <i>for an active opportunity</i>" depends on the status, and a rule that
 * referenced the wrong field would produce a misleading inline error.
 */
public final class OpportunityRules {

    private OpportunityRules() {
    }

    /**
     * @param status the status the opportunity will have; when null it is treated
     *               as OPEN, so a new opportunity cannot be saved with a past date
     */
    public static void validate(BigDecimal amount, Integer probability,
                                LocalDate expectedCloseDate, OpportunityStatus status) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("amount",
                    "Opportunity Amount must be greater than 0.");
        }
        if (probability == null || probability < 0 || probability > 100) {
            throw new BusinessRuleException("probability",
                    "Probability must be between 0 and 100.");
        }
        OpportunityStatus effectiveStatus = status == null ? OpportunityStatus.OPEN : status;
        boolean active = effectiveStatus.isActive();
        if (active && expectedCloseDate != null && expectedCloseDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("expectedCloseDate",
                    "Expected close date cannot be in the past for an active opportunity.");
        }
    }
}
