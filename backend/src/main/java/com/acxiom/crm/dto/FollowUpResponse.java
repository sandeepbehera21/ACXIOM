package com.acxiom.crm.dto;

import com.acxiom.crm.entity.FollowUp;
import com.acxiom.crm.entity.FollowUpStatus;
import com.acxiom.crm.entity.FollowUpType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FollowUpResponse(
        Long id,
        String subject,
        LocalDate followUpDate,
        FollowUpType followUpType,
        String remarks,
        FollowUpStatus status,
        Long customerId,
        String customerName,
        Long leadId,
        String leadName,
        Long assignedToId,
        String assignedToName,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate
) {
    public static FollowUpResponse from(FollowUp followUp) {
        return new FollowUpResponse(
                followUp.getId(),
                followUp.getSubject(),
                followUp.getFollowUpDate(),
                followUp.getFollowUpType(),
                followUp.getRemarks(),
                followUp.getStatus(),
                followUp.getCustomer() != null ? followUp.getCustomer().getId() : null,
                followUp.getCustomer() != null ? followUp.getCustomer().getName() : null,
                followUp.getLead() != null ? followUp.getLead().getId() : null,
                followUp.getLead() != null ? followUp.getLead().getName() : null,
                followUp.getAssignedTo() != null ? followUp.getAssignedTo().getId() : null,
                followUp.getAssignedTo() != null ? followUp.getAssignedTo().getName() : null,
                followUp.getCreatedDate(),
                followUp.getModifiedDate()
        );
    }
}
