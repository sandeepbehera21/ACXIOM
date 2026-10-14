package com.acxiom.crm.dto;

import com.acxiom.crm.entity.Activity;
import com.acxiom.crm.entity.ActivityStatus;
import com.acxiom.crm.entity.ActivityType;
import java.time.LocalDateTime;

public record ActivityResponse(
        Long id,
        ActivityType activityType,
        String subject,
        String description,
        LocalDateTime activityDate,
        ActivityStatus status,
        Long customerId,
        String customerName,
        Long leadId,
        String leadName,
        Long assignedToId,
        String assignedToName,
        LocalDateTime createdDate,
        LocalDateTime modifiedDate
) {
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getActivityType(),
                activity.getSubject(),
                activity.getDescription(),
                activity.getActivityDate(),
                activity.getStatus(),
                activity.getCustomer() != null ? activity.getCustomer().getId() : null,
                activity.getCustomer() != null ? activity.getCustomer().getName() : null,
                activity.getLead() != null ? activity.getLead().getId() : null,
                activity.getLead() != null ? activity.getLead().getName() : null,
                activity.getAssignedTo() != null ? activity.getAssignedTo().getId() : null,
                activity.getAssignedTo() != null ? activity.getAssignedTo().getName() : null,
                activity.getCreatedDate(),
                activity.getModifiedDate()
        );
    }
}
