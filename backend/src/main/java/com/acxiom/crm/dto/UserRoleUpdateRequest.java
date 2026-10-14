package com.acxiom.crm.dto;

import com.acxiom.crm.entity.RoleName;
import jakarta.validation.constraints.NotNull;

/** Admin-only role change (p.7 §4.7, p.9 §7.1). */
public record UserRoleUpdateRequest(
        @NotNull(message = "Role is required.")
        RoleName role) {
}
