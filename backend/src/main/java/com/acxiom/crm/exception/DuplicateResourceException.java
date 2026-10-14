package com.acxiom.crm.exception;

/**
 * Maps to HTTP 409.
 * <p>
 * Used for the duplicate-prevention rules required by p.15 §17.5
 * ("Email uniqueness must be enforced", "Phone uniqueness must be enforced",
 * "Duplicate customer creation must be prevented").
 */
public class DuplicateResourceException extends RuntimeException {

    private final String field;

    public DuplicateResourceException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
