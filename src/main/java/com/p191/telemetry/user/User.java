package com.p191.telemetry.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")          // 'users' — KHÔNG 'user' (reserved word Postgres)
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;     // BCrypt hash, không bao giờ lưu plaintext

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // getters/setters (bỏ bớt cho gọn — sinh trong IDE)
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }
    public String getPassword() { return password; }
    public void setPassword(String p) { this.password = p; }
    public Role getRole() { return role; }
    public void setRole(Role r) { this.role = r; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean e) { this.enabled = e; }
}