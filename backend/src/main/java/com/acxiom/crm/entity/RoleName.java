package com.acxiom.crm.entity;

/**
 * The three roles named in the specification (p.9 §7 Role-Based Authorization).
 * A user holds exactly one of these (specification §4.7: "Assign exactly one
 * primary role ... unless multi-role support is explicitly implemented").
 */
public enum RoleName {
    ADMIN("Admin"),
    MANAGER("Manager"),
    SALES_EXECUTIVE("Sales Executive");

    private final String displayName;

    RoleName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
