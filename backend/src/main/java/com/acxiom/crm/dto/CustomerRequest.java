package com.acxiom.crm.dto;

import com.acxiom.crm.entity.CustomerStatus;
import com.acxiom.crm.validation.ValidPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Create/update payload for a customer.
 * <p>
 * Server-side validation is mandatory even though the UI validates too, because
 * p.7 §5.2 requires: "Repeat all security- and business-critical validations on
 * the server" and "Reject unauthorized record modifications even when a user
 * manipulates browser requests."
 * <p>
 * Length limits mirror the database columns and the client-side rules required
 * by p.7 §5.1 (Length) and p.15 §17.5.
 */
public record CustomerRequest(
        @NotBlank(message = "Customer name is required.")
        @Size(max = 150, message = "Customer name must not exceed 150 characters.")
        String name,

        @NotBlank(message = "Email is required.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 150, message = "Email must not exceed 150 characters.")
        String email,

        @NotBlank(message = "Phone is required.")
        @ValidPhone
        String phone,

        @Size(max = 150, message = "Company name must not exceed 150 characters.")
        String companyName,

        @Size(max = 255, message = "Address must not exceed 255 characters.")
        String address,

        @Size(max = 100, message = "City must not exceed 100 characters.")
        String city,

        @Size(max = 100, message = "State must not exceed 100 characters.")
        String state,

        CustomerStatus status,

        @Size(max = 1000, message = "Notes must not exceed 1000 characters.")
        String notes,

        /** Assigned sales executive. Null keeps the existing owner. */
        Long ownerId) {
}
