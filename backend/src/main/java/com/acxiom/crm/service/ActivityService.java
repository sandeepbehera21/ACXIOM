package com.acxiom.crm.service;

import com.acxiom.crm.audit.AuditService;
import com.acxiom.crm.dto.ActivityRequest;
import com.acxiom.crm.dto.ActivityResponse;
import com.acxiom.crm.entity.Activity;
import com.acxiom.crm.entity.ActivityStatus;
import com.acxiom.crm.entity.ActivityType;
import com.acxiom.crm.entity.AppUser;
import com.acxiom.crm.entity.AuditAction;
import com.acxiom.crm.entity.Customer;
import com.acxiom.crm.entity.Lead;
import com.acxiom.crm.exception.ResourceNotFoundException;
import com.acxiom.crm.repository.ActivityRepository;
import com.acxiom.crm.repository.AppUserRepository;
import com.acxiom.crm.repository.CustomerRepository;
import com.acxiom.crm.repository.LeadRepository;
import com.acxiom.crm.security.CurrentUserService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final AppUserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public ActivityService(ActivityRepository activityRepository,
                           CustomerRepository customerRepository,
                           LeadRepository leadRepository,
                           AppUserRepository userRepository,
                           CurrentUserService currentUserService,
                           AuditService auditService) {
        this.activityRepository = activityRepository;
        this.customerRepository = customerRepository;
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> search(
            ActivityType activityType,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            ActivityStatus status,
            Long assignedToId,
            Long customerId,
            Long leadId) {
        Long effectiveAssignedId = currentUserService.canSeeAllRecords()
                ? assignedToId
                : currentUserService.requireCurrentUser().getId();

        return activityRepository.findAll(
                ActivityRepository.filter(activityType, dateFrom, dateTo, status, effectiveAssignedId, customerId, leadId),
                Sort.by(Sort.Direction.DESC, "activityDate")
        ).stream().map(ActivityResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ActivityResponse findById(Long id) {
        Activity activity = requireActivity(id);
        currentUserService.requireAccessTo(
                activity.getAssignedTo() != null ? activity.getAssignedTo().getId() : null,
                "Activity", id);
        return ActivityResponse.from(activity);
    }

    @Transactional
    public ActivityResponse create(ActivityRequest request) {
        AppUser current = currentUserService.requireCurrentUser();
        AppUser assignedTo = resolveAssignedTo(request.assignedToId(), current);

        Activity activity = new Activity();
        activity.setActivityType(request.activityType());
        activity.setSubject(request.subject().trim());
        activity.setDescription(request.description() != null ? request.description().trim() : null);
        activity.setActivityDate(request.activityDate() != null ? request.activityDate() : LocalDateTime.now());
        activity.setStatus(request.status() != null ? request.status() : ActivityStatus.PLANNED);
        activity.setAssignedTo(assignedTo);

        if (request.customerId() != null) {
            Customer customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + request.customerId()));
            activity.setCustomer(customer);
        }

        if (request.leadId() != null) {
            Lead lead = leadRepository.findById(request.leadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id " + request.leadId()));
            activity.setLead(lead);
        }

        Activity saved = activityRepository.save(activity);

        auditService.record(current, "Activity", AuditAction.CREATE, "Activity",
                String.valueOf(saved.getId()), "SUCCESS",
                "Recorded activity: " + saved.getActivityType() + " - " + saved.getSubject());

        return ActivityResponse.from(saved);
    }

    @Transactional
    public ActivityResponse update(Long id, ActivityRequest request) {
        Activity activity = requireActivity(id);
        AppUser current = currentUserService.requireCurrentUser();
        currentUserService.requireAccessTo(
                activity.getAssignedTo() != null ? activity.getAssignedTo().getId() : null,
                "Activity", id);

        activity.setActivityType(request.activityType());
        activity.setSubject(request.subject().trim());
        activity.setDescription(request.description() != null ? request.description().trim() : null);
        if (request.activityDate() != null) {
            activity.setActivityDate(request.activityDate());
        }
        if (request.status() != null) {
            activity.setStatus(request.status());
        }
        activity.setModifiedDate(LocalDateTime.now());

        if (currentUserService.canSeeAllRecords() && request.assignedToId() != null) {
            AppUser assignedTo = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found: " + request.assignedToId()));
            activity.setAssignedTo(assignedTo);
        }

        Activity saved = activityRepository.save(activity);

        auditService.record(current, "Activity", AuditAction.UPDATE, "Activity",
                String.valueOf(saved.getId()), "SUCCESS",
                "Updated activity: " + saved.getSubject() + " (status=" + saved.getStatus() + ")");

        return ActivityResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Activity activity = requireActivity(id);
        AppUser current = currentUserService.requireCurrentUser();
        currentUserService.requireAccessTo(
                activity.getAssignedTo() != null ? activity.getAssignedTo().getId() : null,
                "Activity", id);

        activityRepository.delete(activity);

        auditService.record(current, "Activity", AuditAction.DELETE, "Activity",
                String.valueOf(id), "SUCCESS",
                "Deleted activity: " + activity.getSubject());
    }

    private AppUser resolveAssignedTo(Long requestedId, AppUser current) {
        if (!currentUserService.canSeeAllRecords() || requestedId == null) {
            return current;
        }
        return userRepository.findById(requestedId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found with id " + requestedId));
    }

    private Activity requireActivity(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id " + id));
    }
}
