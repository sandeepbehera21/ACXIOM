package com.acxiom.crm.controller;

import com.acxiom.crm.dto.LoginRequest;
import com.acxiom.crm.dto.RegisterRequest;
import com.acxiom.crm.dto.UserResponse;
import com.acxiom.crm.entity.AppUser;
import com.acxiom.crm.security.CurrentUserService;
import com.acxiom.crm.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints (p.11 §10).
 * <p>
 * All four are JSON: the frontend is a React SPA, so there is no server-rendered
 * login form to post to.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    public AuthController(AuthService authService, CurrentUserService currentUserService) {
        this.authService = authService;
        this.currentUserService = currentUserService;
    }

    /**
     * The CSRF token, for a client that wants it before its first state-changing
     * request. The token is also written to the {@code XSRF-TOKEN} cookie on every
     * response, so this endpoint is a convenience rather than a requirement.
     */
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request,
                              HttpServletRequest httpRequest) {
        return authService.login(request, httpRequest);
    }

    /**
     * Self-registration. Always creates a Sales Executive - the request body has
     * no role field, so a caller cannot escalate by editing the payload.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        authService.logout(httpRequest);
        return ResponseEntity.noContent().build();
    }

    /** Who am I? Used by the SPA on load to restore the session. */
    @GetMapping("/me")
    public UserResponse me() {
        AppUser user = currentUserService.requireCurrentUser();
        return UserResponse.from(user);
    }
}
