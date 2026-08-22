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
    private final String saUser, saPass;

    public DataSeeder(RoleRepository roles, UserRepository users, PasswordEncoder encoder,
                      @Value("${app.super-admin.username:superadmin}") String saUser,
                      @Value("${app.super-admin.password:change-me}") String saPass) {
        this.roles = roles; this.users = users; this.encoder = encoder;
        this.saUser = saUser; this.saPass = saPass;
    }

    @Override @Transactional
    public void run(ApplicationArguments args) {
        for (RoleName rn : RoleName.values())              // tạo đủ 3 role nếu thiếu
            roles.findByName(rn).orElseGet(() -> roles.save(new Role(rn)));

        if (!users.existsByUsername(saUser)) {             // seed 1 super admin
            Role sa = roles.findByName(RoleName.SUPER_ADMIN).orElseThrow();
            User u = new User();
            u.setUsername(saUser);
            u.setPassword(encoder.encode(saPass));
            u.setRole(sa);
            users.save(u);
        }
    }
}