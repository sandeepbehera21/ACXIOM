package com.acxiom.crm.service;

import com.acxiom.crm.audit.AuditService;
import com.acxiom.crm.dto.OpportunityRequest;
import com.acxiom.crm.dto.OpportunityResponse;
import com.acxiom.crm.entity.AppUser;
import com.acxiom.crm.entity.AuditAction;
import com.acxiom.crm.entity.Customer;
import com.acxiom.crm.entity.Lead;
import com.acxiom.crm.entity.Opportunity;
import com.acxiom.crm.entity.OpportunityStage;
import com.acxiom.crm.entity.OpportunityStatus;
import com.acxiom.crm.exception.BusinessRuleException;
import com.acxiom.crm.exception.ResourceNotFoundException;
import com.acxiom.crm.repository.AppUserRepository;
import com.acxiom.crm.repository.CustomerRepository;
import com.acxiom.crm.repository.LeadRepository;
import com.acxiom.crm.repository.OpportunityRepository;
import com.acxiom.crm.security.CurrentUserService;
import com.acxiom.crm.validation.OpportunityRules;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Opportunity management (p.4-5 §4.8, p.16 §17.7).
 * <p>
 * The business rules - amount greater than 0, probability between 0 and 100
 * inclusive, and no past close date on an active opportunity - are applied by
 * {@link OpportunityRules} on every write path, including conversion from a lead.
 */
@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final AppUserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public OpportunityService(OpportunityRepository opportunityRepository,
                              CustomerRepository customerRepository,
                              LeadRepository leadRepository,
                              AppUserRepository userRepository,
                              CurrentUserService currentUserService,
                              AuditService auditService) {
        this.opportunityRepository = opportunityRepository;
        this.customerRepository = customerRepository;
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<OpportunityResponse> search(String search, OpportunityStage stage,
                                            OpportunityStatus status, Long ownerId) {
        Long effectiveOwnerId = currentUserService.canSeeAllRecords()
                ? ownerId
                : currentUserService.requireCurrentUser().getId();

        return opportunityRepository
                .findAll(OpportunityRepository.filter(search, stage, status, effectiveOwnerId),
                        Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(OpportunityResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OpportunityResponse findById(Long id) {
        Opportunity opportunity = requireOpportunity(id);
        currentUserService.requireAccessTo(ownerIdOf(opportunity), "Opportunity", id);
        return OpportunityResponse.from(opportunity);
    }

    @Transactional
    public OpportunityResponse create(OpportunityRequest request) {
        AppUser current = currentUserService.requireCurrentUser();
        Customer customer = requireAccessibleCustomer(request.customerId());

        OpportunityStatus status = LeadService.statusForStage(request.stage(), request.status());
        // Validated before anything is written, so a rejected opportunity leaves no
        // partial row behind.
        OpportunityRules.validate(request.amount(), request.probability(),
                request.expectedCloseDate(), status);

        Opportunity opportunity = new Opportunity();
        apply(opportunity, request, customer, status);
        opportunity.setOwner(resolveOwner(request.ownerId(), current));

        Opportunity saved = opportunityRepository.save(opportunity);
        auditService.record(current, "Opportunity", AuditAction.CREATE, "Opportunity",
                String.valueOf(saved.getId()), "SUCCESS",
                "Created opportunity '" + saved.getName() + "' for customer "
                        + customer.getCustomerCode() + ".");
        return OpportunityResponse.from(saved);
    }

    @Transactional
    public OpportunityResponse update(Long id, OpportunityRequest request) {
        Opportunity opportunity = requireOpportunity(id);
        currentUserService.requireAccessTo(ownerIdOf(opportunity), "Opportunity", id);

        AppUser current = currentUserService.requireCurrentUser();
        Customer customer = requireAccessibleCustomer(request.customerId());

        OpportunityStatus status = LeadService.statusForStage(request.stage(), request.status());
        OpportunityRules.validate(request.amount(), request.probability(),
                request.expectedCloseDate(), status);

        String before = snapshot(opportunity);
        OpportunityStage previousStage = opportunity.getStage();

        apply(opportunity, request, customer, status);
        if (request.ownerId() != null) {
            opportunity.setOwner(resolveOwner(request.ownerId(), current));
        }
        opportunity.setModifiedDate(LocalDateTime.now());

        Opportunity saved = opportunityRepository.save(opportunity);

        if (previousStage != saved.getStage()) {
            auditService.record(current, "Opportunity", AuditAction.UPDATE, "Opportunity",
                    String.valueOf(id), "SUCCESS",
                    "Opportunity '" + saved.getName() + "' stage changed from "
                            + previousStage + " to " + saved.getStage()
                            + " (status " + saved.getStatus() + ").");
        }

        auditService.record(current, "Opportunity", AuditAction.UPDATE, "Opportunity",
                String.valueOf(id), "SUCCESS",
                "Updated opportunity '" + saved.getName() + "'.", before, snapshot(saved));
        return OpportunityResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Opportunity opportunity = requireOpportunity(id);
        currentUserService.requireAccessTo(ownerIdOf(opportunity), "Opportunity", id);

        AppUser current = currentUserService.requireCurrentUser();
        String name = opportunity.getName();
        String before = snapshot(opportunity);
        opportunityRepository.delete(opportunity);
        auditService.record(current, "Opportunity", AuditAction.DELETE, "Opportunity",
                String.valueOf(id), "SUCCESS",
                "Deleted opportunity '" + name + "'.", before, null);
    }

    // ---------------------------------------------------------------- helpers

    private Opportunity requireOpportunity(Long id) {
        return opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Opportunity not found with id " + id));
    }

    private Long ownerIdOf(Opportunity opportunity) {
        return opportunity.getOwner() == null ? null : opportunity.getOwner().getId();
    }

    /**
     * An opportunity always belongs to a customer, so the customer itself must be
     * one the caller is allowed to see - otherwise a Sales Executive could attach
     * an opportunity to another executive's customer.
     */
    private Customer requireAccessibleCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id " + customerId));
        currentUserService.requireAccessTo(
                customer.getOwner() == null ? null : customer.getOwner().getId(),
                "Customer", customerId);
        return customer;
    }

    private void apply(Opportunity opportunity, OpportunityRequest request,
                       Customer customer, OpportunityStatus status) {
        opportunity.setName(request.name().trim());
        opportunity.setCustomer(customer);
        opportunity.setLead(resolveLead(request.leadId()));
        opportunity.setAmount(request.amount());
        opportunity.setStage(request.stage());
        opportunity.setStatus(status);
        // Won is certain and lost is impossible, so the probability is derived
        // rather than trusted from the request when the stage is terminal.
        opportunity.setProbability(
                LeadService.probabilityForStatus(status, request.probability()));
        opportunity.setExpectedCloseDate(request.expectedCloseDate());
        opportunity.setSource(trimToNull(request.source()));
        opportunity.setNotes(trimToNull(request.notes()));
    }

    private Lead resolveLead(Long leadId) {
        if (leadId == null) {
            return null;
        }
        return leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id " + leadId));
    }

    private AppUser resolveOwner(Long requestedOwnerId, AppUser current) {
        if (requestedOwnerId == null) {
            return current;
        }
        if (!currentUserService.canSeeAllRecords() && !requestedOwnerId.equals(current.getId())) {
            throw new BusinessRuleException("ownerId",
                    "Sales Executives can only assign records to themselves.");
        }
        return userRepository.findById(requestedOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id " + requestedOwnerId));
    }

    private String snapshot(Opportunity opportunity) {
        return "name=" + opportunity.getName()
                + ", customerId=" + (opportunity.getCustomer() == null ? null : opportunity.getCustomer().getId())
                + ", amount=" + opportunity.getAmount()
                + ", stage=" + opportunity.getStage()
                + ", status=" + opportunity.getStatus()
                + ", probability=" + opportunity.getProbability()
                + ", expectedCloseDate=" + opportunity.getExpectedCloseDate()
                + ", ownerId=" + ownerIdOf(opportunity);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
