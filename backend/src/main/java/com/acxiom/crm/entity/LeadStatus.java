package com.acxiom.crm.entity;

/**
 * Lead statuses, exactly as enumerated in the specification (p.5 §4.5):
 * "New, Contacted, Qualified, Unqualified, Converted and Lost".
 */
public enum LeadStatus {
    NEW,
    CONTACTED,
    QUALIFIED,
    UNQUALIFIED,
    CONVERTED,
    LOST
}
