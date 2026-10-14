package com.acxiom.crm.controller;

import com.acxiom.crm.dto.FollowUpRequest;
import com.acxiom.crm.dto.FollowUpResponse;
import com.acxiom.crm.entity.FollowUpStatus;
import com.acxiom.crm.service.FollowUpService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
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

@RestController
@RequestMapping("/api/followups")
public class FollowUpController {

    private final FollowUpService followUpService;

    public FollowUpController(FollowUpService followUpService) {
        this.followUpService = followUpService;
    }

    @GetMapping
    public List<FollowUpResponse> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) FollowUpStatus status,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long leadId) {
        return followUpService.search(dateFrom, dateTo, status, assignedToId, customerId, leadId);
    }

    @GetMapping("/{id}")
    public FollowUpResponse findById(@PathVariable Long id) {
        return followUpService.findById(id);
    }

    @PostMapping
    public ResponseEntity<FollowUpResponse> create(@Valid @RequestBody FollowUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(followUpService.create(request));
    }

    @PutMapping("/{id}")
    public FollowUpResponse update(@PathVariable Long id, @Valid @RequestBody FollowUpRequest request) {
        return followUpService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        followUpService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
