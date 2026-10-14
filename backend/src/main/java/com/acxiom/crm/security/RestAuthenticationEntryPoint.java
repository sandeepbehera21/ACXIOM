package com.acxiom.crm.security;

import com.acxiom.crm.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Returns a JSON 401 instead of an HTML redirect.
 * <p>
 * The frontend is a React SPA, not server-rendered views, so an HTML login
 * redirect would be useless to it. This replaces the MVC behaviour described in
 * p.4 §4.2 while keeping the same outcome (unauthenticated requests are
 * rejected), as recorded in docs/STACK-MAPPING.md.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiError.of(
                HttpStatus.UNAUTHORIZED.value(), "Unauthorized",
                "Authentication is required to access this resource.",
                request.getRequestURI()));
    }
}
