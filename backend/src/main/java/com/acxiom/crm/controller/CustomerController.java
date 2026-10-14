package com.acxiom.crm.controller;

import com.acxiom.crm.dto.CustomerRequest;
import com.acxiom.crm.dto.CustomerResponse;
import com.acxiom.crm.entity.CustomerStatus;
import com.acxiom.crm.service.CustomerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer CRUD and search (p.6 §5.1, p.15 §17.5).
 * <p>
 * The controller only translates HTTP to service calls: validation, duplicate
 * prevention and the ownership restriction all live behind it, so no endpoint can
 * bypass them. Every body is bound to a DTO - the JPA entities are never accepted
 * from or returned to the client, which is what keeps the password hash and the
 * internal object graph out of the API (p.15 §17.4).
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /** Search and filter: every parameter is optional (p.6 §5.1). */
    @GetMapping
    public List<CustomerResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) Long ownerId) {
        return customerService.search(search, status, ownerId);
    }

    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable Long id) {
        return customerService.findById(id);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id,
                                   @Valid @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
