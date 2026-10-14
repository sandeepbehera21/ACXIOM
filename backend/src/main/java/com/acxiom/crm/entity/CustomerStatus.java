package com.acxiom.crm.entity;

/**
 * Customer lifecycle status. The specification requires a Status field
 * (p.4 §4.4, p.15 §17.3) and a delete/deactivate endpoint (p.11 §10,
 * "Delete/deactivate customer") but does not enumerate the values. These three
 * are a documented assumption.
 */
public enum CustomerStatus {
    PROSPECT,
    ACTIVE,
    INACTIVE
}
