package com.p191.telemetry.auth;

import com.p191.telemetry.audit.AuditAction;
import com.p191.telemetry.audit.AuditService;
import com.p191.telemetry.config.JwtService;
import com.p191.telemetry.security.UserPrincipal;
import com.p191.telemetry.user.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final AuthenticationManager authManager;
    private final AuditService audit;

    public AuthService(UserRepository users, RoleRepository roles, PasswordEncoder encoder,
                       JwtService jwt, AuthenticationManager authManager, AuditService audit) {
        this.users = users; this.roles = roles; this.encoder = encoder;
        this.jwt = jwt; this.authManager = authManager; this.audit = audit;
    }

    /** Đăng ký công khai — LUÔN ép DRIVER, bỏ qua role client gửi. */
    @Transactional
    public AuthResponse register(RegisterRequest req, String ip) {
        if (users.existsByUsername(req.username())) {
            audit.record("ANONYMOUS", AuditAction.REGISTER, req.username(), "trùng username", ip);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username đã tồn tại");
        }
        Role driver = roles.findByName(RoleName.DRIVER)
                .orElseThrow(() -> new IllegalStateException("Thiếu role DRIVER — seeder chưa chạy"));
        User u = new User();
        u.setUsername(req.username());
        u.setPassword(encoder.encode(req.password()));
        u.setRole(driver);
        users.save(u);
        audit.record(u.getUsername(), AuditAction.REGISTER, u.getUsername(), "role=DRIVER", ip);
        return new AuthResponse(jwt.generateToken(UserPrincipal.fromUser(u)),
                u.getUsername(), RoleName.DRIVER.name());
    }

    public AuthResponse login(LoginRequest req, String ip) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        } catch (AuthenticationException e) {
            audit.record(req.username() == null ? "ANONYMOUS" : req.username(),
                    AuditAction.LOGIN_FAILURE, req.username(), null, ip);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai thông tin đăng nhập");
        }
        User u = users.findByUsername(req.username()).orElseThrow();
        String role = u.getRole().getName().name();
        audit.record(u.getUsername(), AuditAction.LOGIN_SUCCESS, null, "role=" + role, ip);
        return new AuthResponse(jwt.generateToken(UserPrincipal.fromUser(u)), u.getUsername(), role);
    }
}