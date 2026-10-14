package com.acxiom.crm.dto;

import com.acxiom.crm.entity.Customer;
import java.time.LocalDateTime;

/** Customer as returned to the client. Exposes no internal fields. */
public record CustomerResponse(
        Long id,
        String customerCode,
        String name,
        String email,
        String phone,
        String companyName,
        String address,
        String city,
        String state,
        String status,
        Long ownerId,
        String ownerName,
        String notes,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerCode(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCompanyName(),
                customer.getAddress(),
                customer.getCity(),
                customer.getState(),
                customer.getStatus() == null ? null : customer.getStatus().name(),
                customer.getOwner() == null ? null : customer.getOwner().getId(),
                customer.getOwner() == null ? null : customer.getOwner().getName(),
                customer.getNotes(),
                customer.getCreatedDate(),
                customer.getModifiedDate());
    }
}
