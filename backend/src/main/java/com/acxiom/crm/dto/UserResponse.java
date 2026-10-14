package com.acxiom.crm.dto;

import com.acxiom.crm.entity.AppUser;

/**
 * User as returned to the client.
 * <p>
 * Contains no password hash, no lockout internals and no other
 * security-sensitive field, as required by p.5 §4.7 ("Do not expose password
 * hashes or security-sensitive fields in UI/API responses") and p.18 §17.14.
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        String role,
        String roleDisplay) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getRoleName().name(),
                user.getRole().getRoleName().getDisplayName());
    }
}
