package com.acxiom.crm.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * The single error shape returned by every endpoint.
 * <p>
 * Required by p.11 §10.1: "Return consistent error objects without exposing
 * stack traces or database details."
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors,
        String path) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(LocalDateTime.now(), status, error, message, Map.of(), path);
    }

    public static ApiError withFields(int status, String error, String message,
                                      Map<String, String> fieldErrors, String path) {
        return new ApiError(LocalDateTime.now(), status, error, message, fieldErrors, path);
    }
}
