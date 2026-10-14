package com.acxiom.crm.service;

import com.acxiom.crm.audit.AuditService;
import com.acxiom.crm.dto.LeadConversionRequest;
import com.acxiom.crm.dto.LeadConversionResponse;
import com.acxiom.crm.dto.LeadRequest;
import com.acxiom.crm.dto.LeadResponse;
import com.acxiom.crm.dto.OpportunityResponse;
import com.acxiom.crm.dto.CustomerResponse;
import com.acxiom.crm.entity.AppUser;
import com.acxiom.crm.entity.AuditAction;
import com.acxiom.crm.entity.Customer;
import com.acxiom.crm.entity.CustomerStatus;
import com.acxiom.crm.entity.Lead;
import com.acxiom.crm.entity.LeadPriority;
import com.acxiom.crm.entity.LeadStatus;
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
 * Lead management and lead conversion (p.5 §4.5, p.7 §5.2).
 * <p>
 * Conversion is the one operation in Phase 1 that writes to three tables, so it
 * is the operation most in need of a transaction: a lead must never end up marked
 * CONVERTED with no customer behind it, and a customer must never be created for
 * a conversion that then failed. {@code @Transactional} on
 * {@link #convert(Long, LeadConversionRequest)} makes the whole thing atomic -
 * any exception rolls all three writes back (p.19 §17.19 step 12).
 */
@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final OpportunityRepository opportunityRepository;
    private final AppUserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public LeadService(LeadRepository leadRepository,
                       CustomerRepository customerRepository,
                       OpportunityRepository opportunityRepository,
                       AppUserRepository userRepository,
                       CurrentUserService currentUserService,
                       AuditService auditService) {
        this.leadRepository = leadRepository;
        this.customerRepository = customerRepository;
        this.opportunityRepository = opportunityRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> search(String search, LeadStatus status, Long assignedToId) {
        Long effectiveAssigneeId = currentUserService.canSeeAllRecords()
                ? assignedToId
                : currentUserService.requireCurrentUser().getId();

        return leadRepository
                .findAll(LeadRepository.filter(search, status, effectiveAssigneeId),
                        Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(LeadResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeadResponse findById(Long id) {
        Lead lead = requireLead(id);
        currentUserService.requireAccessTo(ownerIdOf(lead), "Lead", id);
        return LeadResponse.from(lead);
    }

    @Transactional
    public LeadResponse create(LeadRequest request) {
        AppUser current = currentUserService.requireCurrentUser();

        Lead lead = new Lead();
        apply(lead, request);
        lead.setLeadCode(nextLeadCode());
        lead.setAssignedTo(resolveAssignee(request.assignedToId(), current));

        Lead saved = leadRepository.save(lead);
        auditService.record(current, "Lead", AuditAction.CREATE, "Lead",
                String.valueOf(saved.getId()), "SUCCESS",
                "Created lead " + saved.getLeadCode() + " (" + saved.getName() + ").");
        return LeadResponse.from(saved);
    }

    @Transactional
    public LeadResponse update(Long id, LeadRequest request) {
        Lead lead = requireLead(id);
        currentUserService.requireAccessTo(ownerIdOf(lead), "Lead", id);

        AppUser current = currentUserService.requireCurrentUser();
        String before = snapshot(lead);
        LeadStatus previousStatus = lead.getStatus();

        apply(lead, request);
        if (request.assignedToId() != null) {
            lead.setAssignedTo(resolveAssignee(request.assignedToId(), current));
        }
        lead.setModifiedDate(LocalDateTime.now());

        Lead saved = leadRepository.save(lead);

        // A status change is an "important status change" under p.5 §4.9 and is
        // recorded as such, separately from the ordinary field update.
        if (previousStatus != saved.getStatus()) {
            auditService.record(current, "Lead", AuditAction.UPDATE, "Lead",
                    String.valueOf(id), "SUCCESS",
                    "Lead " + saved.getLeadCode() + " status changed from "
                            + previousStatus + " to " + saved.getStatus() + ".");
        }

        auditService.record(current, "Lead", AuditAction.UPDATE, "Lead",
                String.valueOf(id), "SUCCESS",
                "Updated lead " + saved.getLeadCode() + ".", before, snapshot(saved));
        return LeadResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Lead lead = requireLead(id);
        currentUserService.requireAccessTo(ownerIdOf(lead), "Lead", id);

        if (lead.isConverted()) {
            // The lead is the only record of where the customer came from.
            throw new BusinessRuleException("id",
                    "A converted lead cannot be deleted because it records the origin of a customer.");
        }

        AppUser current = currentUserService.requireCurrentUser();
        String code = lead.getLeadCode();
        String name = lead.getName();
        leadRepository.delete(lead);
        auditService.record(current, "Lead", AuditAction.DELETE, "Lead",
                String.valueOf(id), "SUCCESS", "Deleted lead " + code + " (" + name + ").");
    }

    /**
     * Converts a qualified lead into a customer, and optionally into an
     * opportunity, in a single transaction (p.5 §4.5, p.8 §8 workflow step 5,
     * p.19 §17.19 step 12).
     * <p>
     * Rules enforced here, each with its own message so the UI can show the error
     * beside the right field:
     * <ul>
     *   <li>a lead can only be converted once;</li>
     *   <li>only a QUALIFIED lead may be converted, matching the documented
     *       workflow, which qualifies a lead before converting it;</li>
     *   <li>when an opportunity is requested, the ordinary opportunity rules apply
     *       unchanged - amount above 0, probability 0-100, and no past close date
     *       on an active opportunity.</li>
     * </ul>
     * If a customer already exists with the lead's email address, that customer is
     * reused instead of failing on the uniqueness rule, and the lead is linked to
     * it - otherwise every conversion of a lead whose contact is already a
     * customer would be impossible.
     */
    @Transactional
    public LeadConversionResponse convert(Long id, LeadConversionRequest request) {
        Lead lead = requireLead(id);
        currentUserService.requireAccessTo(ownerIdOf(lead), "Lead", id);
        AppUser current = currentUserService.requireCurrentUser();

        if (lead.isConverted()) {
            throw new BusinessRuleException("status",
                    "This lead has already been converted.");
        }
        if (lead.getStatus() != LeadStatus.QUALIFIED) {
            throw new BusinessRuleException("status",
                    "Only a QUALIFIED lead can be converted. This lead is " + lead.getStatus() + ".");
        }

        Customer customer = resolveCustomerFor(lead, current);
        Opportunity opportunity = null;

        if (request.createOpportunity()) {
            String opportunityName = request.opportunityName() == null || request.opportunityName().isBlank()
                    ? lead.getName() + " Opportunity"
                    : request.opportunityName().trim();
            OpportunityStage stage = request.stage() == null
                    ? OpportunityStage.QUALIFICATION
                    : request.stage();
            OpportunityStatus status = statusForStage(stage, null);
            OpportunityRules.validate(request.amount(), request.probability(),
                    request.expectedCloseDate(), status);

            opportunity = new Opportunity();
            opportunity.setName(opportunityName);
            opportunity.setCustomer(customer);
            opportunity.setLead(lead);
            opportunity.setAmount(request.amount());
            opportunity.setStage(stage);
            opportunity.setStatus(status);
            opportunity.setProbability(probabilityForStatus(status, request.probability()));
            opportunity.setExpectedCloseDate(request.expectedCloseDate());
            opportunity.setSource(request.source() == null ? lead.getSource() : request.source());
            opportunity.setOwner(current);
            opportunity = opportunityRepository.save(opportunity);

            auditService.record(current, "Opportunity", AuditAction.CREATE, "Opportunity",
                    String.valueOf(opportunity.getId()), "SUCCESS",
                    "Created opportunity from converted lead " + lead.getLeadCode() + ".");
        }

        lead.setStatus(LeadStatus.CONVERTED);
        lead.setConvertedCustomer(customer);
        lead.setConvertedDate(LocalDateTime.now());
        lead.setModifiedDate(LocalDateTime.now());
        Lead saved = leadRepository.save(lead);

        auditService.record(current, "Lead", AuditAction.CONVERT, "Lead",
                String.valueOf(id), "SUCCESS",
                "Converted lead " + saved.getLeadCode() + " to customer "
                        + customer.getCustomerCode()
                        + (opportunity == null ? " (no opportunity)." : " with opportunity.")
                        + " OldStatus=" + LeadStatus.QUALIFIED + " NewStatus=" + LeadStatus.CONVERTED + ".");

        return new LeadConversionResponse(
                CustomerResponse.from(customer),
                opportunity == null ? null : OpportunityResponse.from(opportunity),
                LeadResponse.from(saved));
    }

    // ---------------------------------------------------------------- helpers

    private Lead requireLead(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id " + id));
    }

    private Long ownerIdOf(Lead lead) {
        return lead.getAssignedTo() == null ? null : lead.getAssignedTo().getId();
    }

    /** Reuses an existing customer when the lead's email already belongs to one. */
    private Customer resolveCustomerFor(Lead lead, AppUser current) {
        if (lead.getEmail() != null && !lead.getEmail().isBlank()) {
            Customer existing = customerRepository.findByEmailIgnoreCase(lead.getEmail().trim().toLowerCase())
                    .orElse(null);
            if (existing != null) {
                return existing;
            }
        }

        Customer customer = new Customer();
        customer.setName(lead.getName());
        // A customer requires both email and phone, but a lead may carry neither.
        // The lead code supplies a stable stand-in so the customer row is still
        // valid, and the placeholder is obvious to whoever opens the record.
        customer.setEmail(lead.getEmail() == null || lead.getEmail().isBlank()
                ? lead.getLeadCode().toLowerCase() + "@no-email.local"
                : lead.getEmail().trim().toLowerCase());
        customer.setPhone(lead.getPhone() == null || lead.getPhone().isBlank()
                ? placeholderPhone(lead)
                : lead.getPhone().trim());
        customer.setCompanyName(lead.getCompanyName());
        customer.setStatus(CustomerStatus.PROSPECT);
        customer.setNotes(lead.getNotes());
        customer.setCustomerCode(nextCustomerCode());
        customer.setCreatedBy(current);
        customer.setOwner(current);
        return customerRepository.save(customer);
    }

    /**
     * Generates a 10-digit placeholder phone beginning with 9 (the only shape the
     * phone rule accepts) from the lead id, so a lead without a phone number can
     * still be converted. Real data is expected from the "complete the customer
     * details" step that follows conversion.
     */
    private String placeholderPhone(Lead lead) {
        long seed = lead.getId() == null ? 0L : lead.getId();
        return String.format("9%09d", seed % 1_000_000_000L);
    }

    private AppUser resolveAssignee(Long requestedUserId, AppUser current) {
        if (requestedUserId == null) {
            return current;
        }
        if (!currentUserService.canSeeAllRecords() && !requestedUserId.equals(current.getId())) {
            throw new BusinessRuleException("assignedToId",
                    "Sales Executives can only assign leads to themselves.");
        }
        return userRepository.findById(requestedUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id " + requestedUserId));
    }

    /**
     * Won and lost are terminal outcomes, so the stage drives the status and the
     * two cannot contradict each other (e.g. a WON stage still OPEN).
     */
    static OpportunityStatus statusForStage(OpportunityStage stage, OpportunityStatus requested) {
        if (stage == OpportunityStage.WON) {
            return OpportunityStatus.WON;
        }
        if (stage == OpportunityStage.LOST) {
            return OpportunityStatus.LOST;
        }
        return requested == null ? OpportunityStatus.OPEN : requested;
    }

    /** A won deal is certain and a lost one impossible, so probability follows. */
    static int probabilityForStatus(OpportunityStatus status, Integer requested) {
        if (status == OpportunityStatus.WON) {
            return 100;
        }
        if (status == OpportunityStatus.LOST) {
            return 0;
        }
        return requested == null ? 0 : requested;
    }

    private void apply(Lead lead, LeadRequest request) {
        lead.setName(request.name().trim());
        lead.setEmail(trimToNull(request.email()));
        lead.setPhone(trimToNull(request.phone()));
        lead.setCompanyName(trimToNull(request.companyName()));
        lead.setSource(trimToNull(request.source()));
        // The enum type is the status whitelist: an unknown value is rejected by
        // Jackson before this point, so an invalid status can never be stored.
        lead.setStatus(request.status());
        lead.setPriority(request.priority() == null ? LeadPriority.MEDIUM : request.priority());
        lead.setExpectedValue(request.expectedValue());
        lead.setNotes(trimToNull(request.notes()));
    }

    private String nextLeadCode() {
        long next = leadRepository.findFirstByOrderByIdDesc()
                .map(l -> l.getId() + 1)
                .orElse(1L);
        return String.format("LED-%06d", next);
    }

    private String nextCustomerCode() {
        long next = customerRepository.findFirstByOrderByIdDesc()
                .map(c -> c.getId() + 1)
                .orElse(1L);
        return String.format("CUS-%06d", next);
    }

    private String snapshot(Lead lead) {
        return "code=" + lead.getLeadCode()
                + ", name=" + lead.getName()
                + ", status=" + lead.getStatus()
                + ", priority=" + lead.getPriority()
                + ", expectedValue=" + lead.getExpectedValue()
                + ", assignedToId=" + ownerIdOf(lead);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
