package com.acxiom.crm.dto;

import com.acxiom.crm.entity.Opportunity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Opportunity as returned to the client, including the derived weighted amount
 * required by p.5 §4.8 ("Amount x Probability / 100").
 */
public record OpportunityResponse(
        Long id,
        String name,
        Long customerId,
        String customerName,
        Long leadId,
        BigDecimal amount,
        String stage,
        String status,
        Integer probability,
        LocalDate expectedCloseDate,
        String source,
        String notes,
        Long ownerId,
        String ownerName,
        BigDecimal weightedAmount,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate) {

    public static OpportunityResponse from(Opportunity opportunity) {
        return new OpportunityResponse(
                opportunity.getId(),
                opportunity.getName(),
                opportunity.getCustomer() == null ? null : opportunity.getCustomer().getId(),
                opportunity.getCustomer() == null ? null : opportunity.getCustomer().getName(),
                opportunity.getLead() == null ? null : opportunity.getLead().getId(),
                opportunity.getAmount(),
                opportunity.getStage() == null ? null : opportunity.getStage().name(),
                opportunity.getStatus() == null ? null : opportunity.getStatus().name(),
                opportunity.getProbability(),
                opportunity.getExpectedCloseDate(),
                opportunity.getSource(),
                opportunity.getNotes(),
                opportunity.getOwner() == null ? null : opportunity.getOwner().getId(),
                opportunity.getOwner() == null ? null : opportunity.getOwner().getName(),
                opportunity.getWeightedAmount(),
                opportunity.getCreatedDate(),
                opportunity.getModifiedDate());
    }
}
