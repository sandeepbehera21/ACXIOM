package com.acxiom.crm.exception;

/**
 * Maps to HTTP 400.
 * <p>
 * Used for business rules that Bean Validation annotations cannot express,
 * for example the Opportunity rules on p.7 §5.3 that depend on the record's
 * status, or an illegal lead status transition (p.5 §4.5).
 */
public class BusinessRuleException extends RuntimeException {

    private final String field;

    public BusinessRuleException(String message) {
        this(null, message);
    }

    public BusinessRuleException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
