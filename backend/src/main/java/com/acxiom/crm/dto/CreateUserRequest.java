package com.acxiom.crm.dto;

import com.acxiom.crm.entity.RoleName;
import com.acxiom.crm.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Name is required.")
        @Size(max = 100, message = "Name must be at most 100 characters.")
        String name,

        @NotBlank(message = "Email is required.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 150, message = "Email must be at most 150 characters.")
        String email,

        @NotBlank(message = "Password is required.")
        @StrongPassword
        String password,

        @NotNull(message = "Role is required.")
        RoleName role
) {}
