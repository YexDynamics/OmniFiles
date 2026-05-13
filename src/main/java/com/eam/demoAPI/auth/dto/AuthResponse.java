package com.eam.demoAPI.auth.dto;

import java.util.List;

/**
 * Respuesta del login con el token JWT y datos del usuario.
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String email,
        List<String> roles
) {}
