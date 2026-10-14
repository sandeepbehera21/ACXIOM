package com.acxiom.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Login payload. p.4 §4.2: "Authenticate users using username/email and password." */
public record LoginRequest(
        @NotBlank(message = "Email is required.")
        @Email(message = "Enter a valid email address.")
        String email,

        @NotBlank(message = "Password is required.")
        String password) {
}
