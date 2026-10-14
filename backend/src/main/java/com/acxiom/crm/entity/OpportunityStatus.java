package com.acxiom.crm.entity;

/**
 * Opportunity lifecycle status, separate from the pipeline stage.
 * <p>
 * The specification's §17.3 table lists both {@code Stage} and {@code Status}
 * for Opportunity but does not enumerate Status. "Active opportunity" (used by
 * the Amount &gt; 0 and Expected Close Date rules in §4.8 and §17.7) is read as
 * {@link #OPEN}. This is a documented assumption, not a value taken from the
 * specification.
 */
public enum OpportunityStatus {
    OPEN,
    WON,
    LOST;

    /** An opportunity is "active" while it is still open. */
    public boolean isActive() {
        return this == OPEN;
    }
}
