package com.acxiom.crm.dto;

import com.acxiom.crm.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Self-registration payload.
 * <p>
 * p.14 §17.2 and p.19 §17.19 require a working Register, while p.5 §4.7 and the
 * p.9 §7.1 permission matrix say only Admin manages users. The project
 * reconciles the two by allowing self-registration to create Sales Executive
 * accounts ONLY - the least-privileged role. There is deliberately no role
 * field on this DTO, so a caller cannot request a higher role.
 */
public record RegisterRequest(
        @NotBlank(message = "Name is required.")
        @Size(max = 100, message = "Name must not exceed 100 characters.")
        String name,

        @NotBlank(message = "Email is required.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 150, message = "Email must not exceed 150 characters.")
        String email,

        @NotBlank(message = "Password is required.")
        @StrongPassword
        String password,

        @NotBlank(message = "Please confirm the password.")
        String confirmPassword) {
}
