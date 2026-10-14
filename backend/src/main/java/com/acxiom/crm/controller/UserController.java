package com.acxiom.crm.controller;

import com.acxiom.crm.dto.CreateUserRequest;
import com.acxiom.crm.dto.UserResponse;
import com.acxiom.crm.dto.UserRoleUpdateRequest;
import com.acxiom.crm.dto.UserStatusUpdateRequest;
import com.acxiom.crm.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User administration (p.7 §4.7, p.9 §7.1).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Used for user list, search, and assignee dropdowns. Admin and Manager have access. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public List<UserResponse> findAll(@RequestParam(required = false) String search) {
        return userService.findAll(search);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse changeRole(@PathVariable Long id,
                                   @Valid @RequestBody UserRoleUpdateRequest request) {
        return userService.changeRole(id, request.role());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateStatus(@PathVariable Long id,
                                     @Valid @RequestBody UserStatusUpdateRequest request) {
        return userService.updateStatus(id, request.active());
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse unlock(@PathVariable Long id) {
        return userService.unlock(id);
    }
}
