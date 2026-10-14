package com.acxiom.crm.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Admin-only activation or deactivation (p.7 §4.7: "activate/deactivate users").
 * <p>
 * A boxed Boolean rather than a primitive, so that a request that omits the field
 * is rejected by validation instead of silently meaning "deactivate".
 */
public record UserStatusUpdateRequest(
        @NotNull(message = "Active flag is required.")
        Boolean active) {
}
