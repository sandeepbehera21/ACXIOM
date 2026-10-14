package com.acxiom.crm.controller;

import com.acxiom.crm.dto.LeadConversionRequest;
import com.acxiom.crm.dto.LeadConversionResponse;
import com.acxiom.crm.dto.LeadRequest;
import com.acxiom.crm.dto.LeadResponse;
import com.acxiom.crm.entity.LeadPriority;
import com.acxiom.crm.entity.LeadStatus;
import com.acxiom.crm.service.LeadService;
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
 * Lead CRUD, assignment and conversion (p.5 §4.5, p.7 §5.2).
 */
@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public List<LeadResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) LeadPriority priority,
            @RequestParam(required = false) Long assignedToId) {
        // Priority filtering is applied after the scoped query so that the SQL
        // specification stays the single place ownership is enforced.
        List<LeadResponse> results = leadService.search(search, status, assignedToId);
        if (priority == null) {
            return results;
        }
        return results.stream()
                .filter(lead -> priority.name().equals(lead.priority()))
                .toList();
    }

    @GetMapping("/{id}")
    public LeadResponse findById(@PathVariable Long id) {
        return leadService.findById(id);
    }

    @PostMapping
    public ResponseEntity<LeadResponse> create(@Valid @RequestBody LeadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leadService.create(request));
    }

    @PutMapping("/{id}")
    public LeadResponse update(@PathVariable Long id, @Valid @RequestBody LeadRequest request) {
        return leadService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leadService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Convert a qualified lead into a customer, and optionally an opportunity
     * (p.5 §4.5, p.19 §17.19 step 12). Runs in one transaction.
     */
    @PostMapping("/{id}/convert")
    public LeadConversionResponse convert(@PathVariable Long id,
                                          @Valid @RequestBody LeadConversionRequest request) {
        return leadService.convert(id, request);
    }
}
