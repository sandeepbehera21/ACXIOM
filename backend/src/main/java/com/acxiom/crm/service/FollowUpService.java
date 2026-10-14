package com.acxiom.crm.service;

import com.acxiom.crm.audit.AuditService;
import com.acxiom.crm.dto.FollowUpRequest;
import com.acxiom.crm.dto.FollowUpResponse;
import com.acxiom.crm.entity.AppUser;
import com.acxiom.crm.entity.AuditAction;
import com.acxiom.crm.entity.Customer;
import com.acxiom.crm.entity.FollowUp;
import com.acxiom.crm.entity.FollowUpStatus;
import com.acxiom.crm.entity.Lead;
import com.acxiom.crm.exception.BusinessRuleException;
import com.acxiom.crm.exception.ResourceNotFoundException;
import com.acxiom.crm.repository.AppUserRepository;
import com.acxiom.crm.repository.CustomerRepository;
import com.acxiom.crm.repository.FollowUpRepository;
import com.acxiom.crm.repository.LeadRepository;
import com.acxiom.crm.security.CurrentUserService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final AppUserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public FollowUpService(FollowUpRepository followUpRepository,
                           CustomerRepository customerRepository,
                           LeadRepository leadRepository,
                           AppUserRepository userRepository,
                           CurrentUserService currentUserService,
                           AuditService auditService) {
        this.followUpRepository = followUpRepository;
        this.customerRepository = customerRepository;
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<FollowUpResponse> search(
            LocalDate dateFrom,
            LocalDate dateTo,
            FollowUpStatus status,
            Long assignedToId,
            Long customerId,
            Long leadId) {
        Long effectiveAssignedId = currentUserService.canSeeAllRecords()
                ? assignedToId
                : currentUserService.requireCurrentUser().getId();

        return followUpRepository.findAll(
                FollowUpRepository.filter(dateFrom, dateTo, status, effectiveAssignedId, customerId, leadId),
                Sort.by(Sort.Direction.ASC, "followUpDate")
        ).stream().map(FollowUpResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public FollowUpResponse findById(Long id) {
        FollowUp followUp = requireFollowUp(id);
        currentUserService.requireAccessTo(
                followUp.getAssignedTo() != null ? followUp.getAssignedTo().getId() : null,
                "FollowUp", id);
        return FollowUpResponse.from(followUp);
    }

    @Transactional
    public FollowUpResponse create(FollowUpRequest request) {
        AppUser current = currentUserService.requireCurrentUser();

        FollowUpStatus status = request.status() != null ? request.status() : FollowUpStatus.PLANNED;
        validateFollowUpDate(request.followUpDate(), status);

        AppUser assignedTo = resolveAssignedTo(request.assignedToId(), current);

        FollowUp followUp = new FollowUp();
        followUp.setSubject(request.subject().trim());
        followUp.setFollowUpDate(request.followUpDate());
        followUp.setFollowUpType(request.followUpType());
        followUp.setRemarks(request.remarks() != null ? request.remarks().trim() : null);
        followUp.setStatus(status);
        followUp.setAssignedTo(assignedTo);

        if (request.customerId() != null) {
            Customer customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + request.customerId()));
            followUp.setCustomer(customer);
        }

        if (request.leadId() != null) {
            Lead lead = leadRepository.findById(request.leadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id " + request.leadId()));
            followUp.setLead(lead);
        }

        FollowUp saved = followUpRepository.save(followUp);

        auditService.record(current, "FollowUp", AuditAction.CREATE, "FollowUp",
                String.valueOf(saved.getId()), "SUCCESS",
                "Created follow-up: " + saved.getSubject() + " scheduled for " + saved.getFollowUpDate());

        return FollowUpResponse.from(saved);
    }

    @Transactional
    public FollowUpResponse update(Long id, FollowUpRequest request) {
        FollowUp followUp = requireFollowUp(id);
        AppUser current = currentUserService.requireCurrentUser();
        currentUserService.requireAccessTo(
                followUp.getAssignedTo() != null ? followUp.getAssignedTo().getId() : null,
                "FollowUp", id);

        FollowUpStatus status = request.status() != null ? request.status() : followUp.getStatus();
        validateFollowUpDate(request.followUpDate(), status);

        followUp.setSubject(request.subject().trim());
        followUp.setFollowUpDate(request.followUpDate());
        followUp.setFollowUpType(request.followUpType());
        followUp.setRemarks(request.remarks() != null ? request.remarks().trim() : null);
        followUp.setStatus(status);
        followUp.setModifiedDate(LocalDateTime.now());

        if (currentUserService.canSeeAllRecords() && request.assignedToId() != null) {
            AppUser assignedTo = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found: " + request.assignedToId()));
            followUp.setAssignedTo(assignedTo);
        }

        FollowUp saved = followUpRepository.save(followUp);

        auditService.record(current, "FollowUp", AuditAction.UPDATE, "FollowUp",
                String.valueOf(saved.getId()), "SUCCESS",
                "Updated follow-up: " + saved.getSubject() + " (status=" + saved.getStatus() + ")");

        return FollowUpResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        FollowUp followUp = requireFollowUp(id);
        AppUser current = currentUserService.requireCurrentUser();
        currentUserService.requireAccessTo(
                followUp.getAssignedTo() != null ? followUp.getAssignedTo().getId() : null,
                "FollowUp", id);

        followUpRepository.delete(followUp);

        auditService.record(current, "FollowUp", AuditAction.DELETE, "FollowUp",
                String.valueOf(id), "SUCCESS",
                "Deleted follow-up: " + followUp.getSubject());
    }

    private void validateFollowUpDate(LocalDate date, FollowUpStatus status) {
        if (date != null && status == FollowUpStatus.PLANNED && date.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("followUpDate", "Follow-up date cannot be earlier than today.");
        }
    }

    private AppUser resolveAssignedTo(Long requestedId, AppUser current) {
        if (!currentUserService.canSeeAllRecords() || requestedId == null) {
            return current;
        }
        return userRepository.findById(requestedId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found with id " + requestedId));
    }

    private FollowUp requireFollowUp(Long id) {
        return followUpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FollowUp not found with id " + id));
    }
}
