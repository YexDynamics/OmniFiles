package com.eam.demoAPI.security.dto;

import java.time.Instant;

/**
 * Respuesta estándar de error de seguridad (401/403).
 */
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {
    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(status, error, message, path, Instant.now());
    }
}
