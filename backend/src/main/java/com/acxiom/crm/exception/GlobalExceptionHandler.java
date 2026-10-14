package com.acxiom.crm.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates exceptions into the consistent {@link ApiError} shape.
 * <p>
 * HTTP status codes follow p.6 §4.10: "Return appropriate HTTP status codes such
 * as 200, 201, 400, 401, 403, 404 and 409 where applicable."
 * <p>
 * The catch-all handler deliberately returns a generic message; p.12 §12 says
 * "Do not expose technical exception messages to end users" and p.11 §10.1 says
 * errors must not expose stack traces or database details. The real cause is
 * logged server-side instead.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Bean Validation failures on a request body -> 400 with per-field messages. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // Keep the first message per field so the UI shows one clear error.
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(ApiError.withFields(
                HttpStatus.BAD_REQUEST.value(), "Validation Failed",
                "One or more fields are invalid. Please correct them and try again.",
                fieldErrors, request.getRequestURI()));
    }

    /** Business rule violation -> 400. */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex,
                                                       HttpServletRequest request) {
        Map<String, String> fields = ex.getField() == null
                ? Map.of()
                : Map.of(ex.getField(), ex.getMessage());
        return ResponseEntity.badRequest().body(ApiError.withFields(
                HttpStatus.BAD_REQUEST.value(), "Business Rule Violation",
                ex.getMessage(), fields, request.getRequestURI()));
    }

    /** Duplicate email/phone/customer -> 409. */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex,
                                                    HttpServletRequest request) {
        Map<String, String> fields = ex.getField() == null
                ? Map.of()
                : Map.of(ex.getField(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.withFields(
                HttpStatus.CONFLICT.value(), "Conflict",
                ex.getMessage(), fields, request.getRequestURI()));
    }

    /** Missing record -> 404. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex,
                                                   HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(
                HttpStatus.NOT_FOUND.value(), "Not Found",
                ex.getMessage(), request.getRequestURI()));
    }

    /** Wrong password -> 401. */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex,
                                                          HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.of(
                HttpStatus.UNAUTHORIZED.value(), "Unauthorized",
                "Invalid email or password.", request.getRequestURI()));
    }

    /** Locked account -> 401 with an explanation (p.8 §6.3). */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiError> handleLocked(LockedException ex,
                                                 HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.of(
                HttpStatus.UNAUTHORIZED.value(), "Account Locked",
                ex.getMessage() == null ? "Account is temporarily locked." : ex.getMessage(),
                request.getRequestURI()));
    }

    /** Deactivated account -> 401. */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabled(DisabledException ex,
                                                   HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.of(
                HttpStatus.UNAUTHORIZED.value(), "Account Disabled",
                "This account has been deactivated.", request.getRequestURI()));
    }

    /** Authenticated but not permitted -> 403 (p.9 §7: enforced server-side). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex,
                                                       HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(
                HttpStatus.FORBIDDEN.value(), "Forbidden",
                "You do not have permission to perform this action.",
                request.getRequestURI()));
    }

    /**
     * A body that cannot be parsed -> 400.
     * <p>
     * This covers malformed JSON and, more usefully, a value that is not valid for
     * its field - an unknown lead status or opportunity stage, for example. Without
     * this handler those cases surface as a 500, which tells the user nothing and
     * wrongly implies the server is broken. The status whitelist is the enum type
     * itself, so this is the response that enforces it.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                         HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof InvalidFormatException invalidFormat
                && !invalidFormat.getPath().isEmpty()) {
            String field = invalidFormat.getPath()
                    .get(invalidFormat.getPath().size() - 1).getFieldName();
            if (field != null) {
                // The rejected value is the caller's own input, so echoing it back
                // is safe and makes the message actionable.
                fieldErrors.put(field,
                        "The value '" + invalidFormat.getValue() + "' is not valid for this field.");
            }
        }
        return ResponseEntity.badRequest().body(ApiError.withFields(
                HttpStatus.BAD_REQUEST.value(), "Malformed Request",
                "The request body could not be read. Please check the values and try again.",
                fieldErrors, request.getRequestURI()));
    }

    /** Anything else -> 500 with no internals leaked. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "Something went wrong processing your request. Please try again.",
                request.getRequestURI()));
    }
}
