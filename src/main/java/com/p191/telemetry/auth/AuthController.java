package com.p191.telemetry.auth;

import com.p191.telemetry.auth.dto.LoginRequest;
import com.p191.telemetry.auth.dto.LoginResponse;
import com.p191.telemetry.security.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AdminUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long ttlSeconds;

    public AuthController(AdminUserRepository users,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          @Value("${app.security.jwt.ttl-seconds}") long ttlSeconds) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.ttlSeconds = ttlSeconds;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        AdminUser user = users.findByUsername(req.username())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tài khoản hoặc mật khẩu"));

        String token = jwtService.generate(user.getUsername(), user.getRole());
        return new LoginResponse(token, "Bearer", ttlSeconds, user.getUsername(), user.getRole());
    }
}
