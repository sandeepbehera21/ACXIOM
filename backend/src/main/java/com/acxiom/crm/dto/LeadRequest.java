package com.acxiom.crm.dto;

import com.acxiom.crm.entity.LeadPriority;
import com.acxiom.crm.entity.LeadStatus;
import com.acxiom.crm.validation.ValidPhone;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Create/update payload for a lead.
 * <p>
 * Validation follows p.15 §17.6 ("Lead name is mandatory", "Expected value must
 * be within the configured numeric range", "Lead status is mandatory and must
 * come from a valid status set", "Lead email and phone must follow the project's
 * common validation rules").
 */
public record LeadRequest(
        @NotBlank(message = "Lead name is required.")
        @Size(max = 150, message = "Lead name must not exceed 150 characters.")
        String name,

        @Email(message = "Enter a valid email address.")
        @Size(max = 150, message = "Email must not exceed 150 characters.")
        String email,

        @ValidPhone
        String phone,

        @Size(max = 150, message = "Company name must not exceed 150 characters.")
        String companyName,

        @Size(max = 50, message = "Source must not exceed 50 characters.")
        String source,

        @NotNull(message = "Lead status is required.")
        LeadStatus status,

        LeadPriority priority,

        @DecimalMin(value = "0.0", message = "Expected value cannot be negative.")
        @Digits(integer = 13, fraction = 2, message = "Expected value must have at most 2 decimal places.")
        BigDecimal expectedValue,

        @Size(max = 1000, message = "Notes must not exceed 1000 characters.")
        String notes,

        /** Assigned sales executive. Null keeps the existing assignee. */
        Long assignedToId) {
}
