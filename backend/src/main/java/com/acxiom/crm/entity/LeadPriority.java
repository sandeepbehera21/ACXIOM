package com.acxiom.crm.entity;

/**
 * Lead priority. The specification requires a priority field (p.5 §4.5
 * "status, priority and notes") but does not enumerate its values, so a plain
 * three-value set is used.
 */
public enum LeadPriority {
    LOW,
    MEDIUM,
    HIGH
}
