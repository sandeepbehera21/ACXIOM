package com.acxiom.crm.entity;

/**
 * Auditable event types.
 * <p>
 * Derived from p.5 §4.9 and p.18 §17.16, which require audit entries for:
 * login success, failed login, logout, create, update, delete, role change and
 * password/security events. The 409-style lockout and unlock events are
 * required by p.8 §6.3 ("Audit lockout and unlock events").
 */
public enum AuditAction {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    LOCKOUT,
    UNLOCK,
    CREATE,
    UPDATE,
    DELETE,
    CONVERT,
    ROLE_CHANGE,
    SECURITY
}
