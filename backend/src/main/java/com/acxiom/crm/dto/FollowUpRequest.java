package com.acxiom.crm.dto;

import com.acxiom.crm.entity.FollowUpType;
import com.acxiom.crm.entity.FollowUpStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record FollowUpRequest(
        @NotBlank(message = "Subject is required.")
        @Size(max = 150, message = "Subject must be at most 150 characters.")
        String subject,

        @NotNull(message = "Follow-up date is required.")
        LocalDate followUpDate,

        @NotNull(message = "Follow-up type is required.")
        FollowUpType followUpType,

        @Size(max = 1000, message = "Remarks must be at most 1000 characters.")
        String remarks,

        FollowUpStatus status,
        Long customerId,
        Long leadId,
        Long assignedToId
) {}
