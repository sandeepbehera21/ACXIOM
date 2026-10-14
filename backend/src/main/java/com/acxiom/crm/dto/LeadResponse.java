package com.acxiom.crm.dto;

import com.acxiom.crm.entity.Lead;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Lead as returned to the client. */
public record LeadResponse(
        Long id,
        String leadCode,
        String name,
        String email,
        String phone,
        String companyName,
        String source,
        String status,
        String priority,
        BigDecimal expectedValue,
        Long assignedToId,
        String assignedToName,
        String notes,
        Long convertedCustomerId,
        LocalDateTime convertedDate,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate) {

    public static LeadResponse from(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getLeadCode(),
                lead.getName(),
                lead.getEmail(),
                lead.getPhone(),
                lead.getCompanyName(),
                lead.getSource(),
                lead.getStatus() == null ? null : lead.getStatus().name(),
                lead.getPriority() == null ? null : lead.getPriority().name(),
                lead.getExpectedValue(),
                lead.getAssignedTo() == null ? null : lead.getAssignedTo().getId(),
                lead.getAssignedTo() == null ? null : lead.getAssignedTo().getName(),
                lead.getNotes(),
                lead.getConvertedCustomer() == null ? null : lead.getConvertedCustomer().getId(),
                lead.getConvertedDate(),
                lead.getCreatedDate(),
                lead.getModifiedDate());
    }
}
