package com.eam.demoAPI.auth.dto;

import java.util.List;

public record AuthResponse(
        Long id,
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String email,
        List<String> roles
) {}