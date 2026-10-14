package com.acxiom.crm.dto;

import com.acxiom.crm.entity.ActivityStatus;
import com.acxiom.crm.entity.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record ActivityRequest(
        @NotNull(message = "Activity type is required.")
        ActivityType activityType,

        @NotBlank(message = "Subject is required.")
        @Size(max = 150, message = "Subject must be at most 150 characters.")
        String subject,

        @Size(max = 1000, message = "Description must be at most 1000 characters.")
        String description,

        LocalDateTime activityDate,
        ActivityStatus status,
        Long customerId,
        Long leadId,
        Long assignedToId
) {}
