package com.p191.telemetry.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.p191.telemetry.audit.AuditAction;
import com.p191.telemetry.audit.AuditService;
import com.p191.telemetry.config.JwtService;
import com.p191.telemetry.security.UserPrincipal;
import com.p191.telemetry.user.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class AuthService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final AuthenticationManager authManager;
    private final AuditService audit;

    @Value("${app.google.oauth-client-id:}")
    private String googleClientId;

    private GoogleIdTokenVerifier googleVerifier;

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

    /**
     * Đăng nhập/đăng ký bằng Google ID token. Verify chữ ký + audience (client ID)
     * qua GoogleIdTokenVerifier — KHÔNG tin payload chưa verify. User mới luôn ép
     * DRIVER, giống register() thường. Lần đầu login gắn google_id vào user có
     * cùng email (username) nếu đã tồn tại, để không tạo trùng tài khoản.
     */
    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest req, String ip) {
        if (googleClientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "GOOGLE_OAUTH_CLIENT_ID chưa được cấu hình trên server");
        }
        GoogleIdToken.Payload payload = verifyGoogleToken(req.idToken());
        String googleId = payload.getSubject();
        Boolean emailVerified = payload.getEmailVerified();
        String email = Boolean.TRUE.equals(emailVerified) ? payload.getEmail() : null;

        User u = users.findByGoogleId(googleId).orElse(null);
        if (u == null && email != null) {
            u = users.findByUsername(email).orElse(null);
            if (u != null) u.setGoogleId(googleId);
        }
        if (u == null) {
            if (email == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token Google thiếu email");
            }
            Role driver = roles.findByName(RoleName.DRIVER)
                    .orElseThrow(() -> new IllegalStateException("Thiếu role DRIVER — seeder chưa chạy"));
            u = new User();
            u.setUsername(email);
            u.setGoogleId(googleId);
            u.setPassword(null);
            u.setRole(driver);
            audit.record(email, AuditAction.REGISTER, email, "role=DRIVER, via=google", ip);
        }
        users.save(u);
        String role = u.getRole().getName().name();
        audit.record(u.getUsername(), AuditAction.LOGIN_SUCCESS, null, "role=" + role + ", via=google", ip);
        return new AuthResponse(jwt.generateToken(UserPrincipal.fromUser(u)), u.getUsername(), role);
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        if (idTokenString == null || idTokenString.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu idToken");
        }
        try {
            if (googleVerifier == null) {
                googleVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                        .setAudience(Collections.singletonList(googleClientId))
                        .build();
            }
            GoogleIdToken token = googleVerifier.verify(idTokenString);
            if (token == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google ID token không hợp lệ");
            }
            return token.getPayload();
        } catch (GeneralSecurityException | java.io.IOException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không verify được Google ID token");
        }
    }
}