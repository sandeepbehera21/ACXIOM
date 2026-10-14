package com.acxiom.crm.service;

import com.acxiom.crm.audit.AuditService;
import com.acxiom.crm.dto.CustomerRequest;
import com.acxiom.crm.dto.CustomerResponse;
import com.acxiom.crm.entity.AppUser;
import com.acxiom.crm.entity.AuditAction;
import com.acxiom.crm.entity.Customer;
import com.acxiom.crm.entity.CustomerStatus;
import com.acxiom.crm.exception.BusinessRuleException;
import com.acxiom.crm.exception.DuplicateResourceException;
import com.acxiom.crm.exception.ResourceNotFoundException;
import com.acxiom.crm.repository.AppUserRepository;
import com.acxiom.crm.repository.CustomerRepository;
import com.acxiom.crm.repository.LeadRepository;
import com.acxiom.crm.repository.OpportunityRepository;
import com.acxiom.crm.security.CurrentUserService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Customer management (p.6 §5.1, p.15 §17.5).
 * <p>
 * Every read and write passes through the ownership check, because the
 * specification requires that a Sales Executive can only reach their own records
 * and that this is enforced on the server rather than by hiding navigation links
 * (p.9 §7, p.12 §13).
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AppUserRepository userRepository;
    private final OpportunityRepository opportunityRepository;
    private final LeadRepository leadRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public CustomerService(CustomerRepository customerRepository,
                           AppUserRepository userRepository,
                           OpportunityRepository opportunityRepository,
                           LeadRepository leadRepository,
                           CurrentUserService currentUserService,
                           AuditService auditService) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.opportunityRepository = opportunityRepository;
        this.leadRepository = leadRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> search(String search, CustomerStatus status, Long ownerId) {
        // A Sales Executive's filter is forced to their own id, so passing another
        // user's id in the query string cannot widen what they see.
        Long effectiveOwnerId = currentUserService.canSeeAllRecords()
                ? ownerId
                : currentUserService.requireCurrentUser().getId();

        return customerRepository
                .findAll(CustomerRepository.filter(search, status, effectiveOwnerId),
                        Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(CustomerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        Customer customer = requireCustomer(id);
        currentUserService.requireAccessTo(ownerIdOf(customer), "Customer", id);
        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        String email = request.email().trim().toLowerCase();
        requireUniqueEmail(email, null);
        requireUniquePhone(request.phone(), null);

        AppUser current = currentUserService.requireCurrentUser();

        Customer customer = new Customer();
        apply(customer, request, email);
        customer.setCustomerCode(nextCustomerCode());
        customer.setCreatedBy(current);
        customer.setOwner(resolveOwner(request.ownerId(), current));

        Customer saved = customerRepository.save(customer);
        auditService.record(current, "Customer", AuditAction.CREATE, "Customer",
                String.valueOf(saved.getId()), "SUCCESS",
                "Created customer " + saved.getCustomerCode() + " (" + saved.getName() + ").");
        return CustomerResponse.from(saved);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = requireCustomer(id);
        currentUserService.requireAccessTo(ownerIdOf(customer), "Customer", id);

        String email = request.email().trim().toLowerCase();
        requireUniqueEmail(email, id);
        requireUniquePhone(request.phone(), id);

        AppUser current = currentUserService.requireCurrentUser();
        String before = snapshot(customer);

        apply(customer, request, email);
        if (request.ownerId() != null) {
            customer.setOwner(resolveOwner(request.ownerId(), current));
        }
        customer.setModifiedDate(LocalDateTime.now());

        Customer saved = customerRepository.save(customer);
        auditService.record(current, "Customer", AuditAction.UPDATE, "Customer",
                String.valueOf(id), "SUCCESS",
                "Updated customer " + saved.getCustomerCode() + ".", before, snapshot(saved));
        return CustomerResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = requireCustomer(id);
        currentUserService.requireAccessTo(ownerIdOf(customer), "Customer", id);

        // A customer with dependent records cannot simply vanish: the opportunity
        // and lead rows would be left pointing at nothing. Blocked with a clear
        // message rather than allowed to fail as a database error.
        if (opportunityRepository.countByCustomerId(id) > 0) {
            throw new BusinessRuleException("id",
                    "This customer has opportunities and cannot be deleted. "
                            + "Delete or reassign the opportunities first.");
        }
        if (leadRepository.countByConvertedCustomerId(id) > 0) {
            throw new BusinessRuleException("id",
                    "This customer was created by converting a lead and cannot be deleted.");
        }

        AppUser current = currentUserService.requireCurrentUser();
        String code = customer.getCustomerCode();
        customerRepository.delete(customer);
        auditService.record(current, "Customer", AuditAction.DELETE, "Customer",
                String.valueOf(id), "SUCCESS", "Deleted customer " + code + " (" + customer.getName() + ").");
    }

    // ---------------------------------------------------------------- helpers

    private Customer requireCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }

    private Long ownerIdOf(Customer customer) {
        return customer.getOwner() == null ? null : customer.getOwner().getId();
    }

    /** §17.5: "Email uniqueness must be enforced". */
    private void requireUniqueEmail(String email, Long selfId) {
        customerRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (selfId == null || !existing.getId().equals(selfId)) {
                throw new DuplicateResourceException("email",
                        "A customer with this email already exists.");
            }
        });
    }

    /** §17.5: "Phone uniqueness must be enforced". */
    private void requireUniquePhone(String phone, Long selfId) {
        customerRepository.findByPhone(phone).ifPresent(existing -> {
            if (selfId == null || !existing.getId().equals(selfId)) {
                throw new DuplicateResourceException("phone",
                        "A customer with this phone number already exists.");
            }
        });
    }

    private void apply(Customer customer, CustomerRequest request, String normalisedEmail) {
        customer.setName(request.name().trim());
        customer.setEmail(normalisedEmail);
        customer.setPhone(request.phone().trim());
        customer.setCompanyName(trimToNull(request.companyName()));
        customer.setAddress(trimToNull(request.address()));
        customer.setCity(trimToNull(request.city()));
        customer.setState(trimToNull(request.state()));
        customer.setNotes(trimToNull(request.notes()));
        customer.setStatus(request.status() == null ? CustomerStatus.PROSPECT : request.status());
    }

    /**
     * Only Admin and Manager may assign a record to somebody else. A Sales
     * Executive creating a customer owns it; attempting to hand it to another user
     * is rejected rather than silently ignored.
     */
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

    /**
     * Sequential business key, e.g. CUS-000001 (p.15 §17.3). The unique constraint
     * on the column is the real guarantee; this only produces the next value.
     */
    private String nextCustomerCode() {
        long next = customerRepository.findFirstByOrderByIdDesc()
                .map(c -> c.getId() + 1)
                .orElse(1L);
        return String.format("CUS-%06d", next);
    }

    /** Compact, non-sensitive summary for the audit old/new columns. */
    private String snapshot(Customer customer) {
        return "code=" + customer.getCustomerCode()
                + ", name=" + customer.getName()
                + ", email=" + customer.getEmail()
                + ", phone=" + customer.getPhone()
                + ", status=" + customer.getStatus()
                + ", ownerId=" + ownerIdOf(customer);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
