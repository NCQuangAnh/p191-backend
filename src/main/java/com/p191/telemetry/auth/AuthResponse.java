package com.p191.telemetry.auth;

public record AuthResponse(String token, String username, String role) {}