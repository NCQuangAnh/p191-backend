package com.p191.telemetry.auth.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        String username,
        String role
) {}
