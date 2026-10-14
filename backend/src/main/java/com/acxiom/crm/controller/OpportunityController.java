package com.acxiom.crm.controller;

import com.acxiom.crm.dto.OpportunityRequest;
import com.acxiom.crm.dto.OpportunityResponse;
import com.acxiom.crm.entity.OpportunityStage;
import com.acxiom.crm.entity.OpportunityStatus;
import com.acxiom.crm.service.OpportunityService;
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
 * Opportunity CRUD (p.4-5 §4.8, p.16 §17.7).
 * <p>
 * Each response carries the weighted amount (Amount x Probability / 100) so the
 * figure is computed once from the stored values and cannot drift from what the
 * pipeline report adds up.
 */
@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping
    public List<OpportunityResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) OpportunityStage stage,
            @RequestParam(required = false) OpportunityStatus status,
            @RequestParam(required = false) Long ownerId) {
        return opportunityService.search(search, stage, status, ownerId);
    }

    @GetMapping("/{id}")
    public OpportunityResponse findById(@PathVariable Long id) {
        return opportunityService.findById(id);
    }

    @PostMapping
    public ResponseEntity<OpportunityResponse> create(
            @Valid @RequestBody OpportunityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(opportunityService.create(request));
    }

    @PutMapping("/{id}")
    public OpportunityResponse update(@PathVariable Long id,
                                      @Valid @RequestBody OpportunityRequest request) {
        return opportunityService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        opportunityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
