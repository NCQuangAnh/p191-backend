package com.p191.telemetry.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;                    // CHỈ AuthService, KHÔNG JwtService
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest req, HttpServletRequest http) {
        return auth.register(req, clientIp(http));
    }
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req, HttpServletRequest http) {
        return auth.login(req, clientIp(http));
    }
    private static String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}