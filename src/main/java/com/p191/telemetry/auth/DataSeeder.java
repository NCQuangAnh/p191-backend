package com.p191.telemetry.auth;

import com.p191.telemetry.user.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements ApplicationRunner {
    private final RoleRepository roles;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String saUser, saPass, adminUser, adminPass;

    public DataSeeder(RoleRepository roles, UserRepository users, PasswordEncoder encoder,
                      @Value("${app.super-admin.username:superadmin}") String saUser,
                      @Value("${app.super-admin.password:change-me}") String saPass,
                      // Tai khoan ADMIN thuong (khac SUPER_ADMIN/"Head Admin") - yeu
                      // cau nguoi dung 24/08: "cho tài khoản superadmin/change-me
                      // thành head admin và admin/admin123 thành admin".
                      @Value("${app.admin.username:admin}") String adminUser,
                      @Value("${app.admin.password:admin123}") String adminPass) {
        this.roles = roles; this.users = users; this.encoder = encoder;
        this.saUser = saUser; this.saPass = saPass;
        this.adminUser = adminUser; this.adminPass = adminPass;
    }

    @Override @Transactional
    public void run(ApplicationArguments args) {
        for (RoleName rn : RoleName.values())              // tạo đủ 3 role nếu thiếu
            roles.findByName(rn).orElseGet(() -> roles.save(new Role(rn)));

        if (!users.existsByUsername(saUser)) {             // seed 1 super admin ("Head Admin")
            Role sa = roles.findByName(RoleName.SUPER_ADMIN).orElseThrow();
            User u = new User();
            u.setUsername(saUser);
            u.setPassword(encoder.encode(saPass));
            u.setRole(sa);
            users.save(u);
        }

        Role adminRole = roles.findByName(RoleName.ADMIN).orElseThrow();
        if (!users.existsByUsername(adminUser)) {          // seed 1 admin thuong
            User u = new User();
            u.setUsername(adminUser);
            u.setPassword(encoder.encode(adminPass));
            u.setRole(adminRole);
            users.save(u);
        } else {
            // Sua role neu DB cu da tung seed "admin" thanh SUPER_ADMIN (cau
            // hinh truoc day: app.super-admin.username mac dinh la "admin" -
            // xem application.yml). Idempotent - chi doi khi sai (yeu cau
            // nguoi dung 24/08: "admin/admin123 thành admin" thuong, khong
            // phai Head Admin).
            users.findByUsername(adminUser).ifPresent(u -> {
                if (u.getRole().getName() != RoleName.ADMIN) {
                    u.setRole(adminRole);
                    users.save(u);
                }
            });
        }
    }
}