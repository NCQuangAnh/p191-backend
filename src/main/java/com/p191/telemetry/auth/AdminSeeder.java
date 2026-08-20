package com.p191.telemetry.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Tạo sẵn 1 admin lúc khởi động nếu chưa tồn tại. Đổi mật khẩu qua biến môi trường / config ở production.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AdminUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminSeeder(AdminUserRepository users,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.admin.username}") String username,
                       @Value("${app.admin.password}") String password) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (users.findByUsername(username).isEmpty()) {
            AdminUser admin = new AdminUser();
            admin.setUsername(username);
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setRole("ADMIN");
            users.save(admin);
            log.info("Đã tạo admin mặc định '{}'. Nhớ đổi mật khẩu ở production.", username);
        }
    }
}
