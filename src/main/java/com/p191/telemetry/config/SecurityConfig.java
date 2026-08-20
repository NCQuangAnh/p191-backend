package com.p191.telemetry.config;

import com.p191.telemetry.security.DeviceApiKeyFilter;
import com.p191.telemetry.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final DeviceApiKeyFilter deviceApiKeyFilter;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(DeviceApiKeyFilter deviceApiKeyFilter, JwtAuthFilter jwtAuthFilter) {
        this.deviceApiKeyFilter = deviceApiKeyFilter;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> {}) // dùng cấu hình CORS mặc định; siết ở production
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(h -> h.frameOptions(f -> f.disable())) // cho H2 console
            .authorizeHttpRequests(auth -> auth
                // Public / self-guarded
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                // WRITE (app khách) — DeviceApiKeyFilter tự kiểm tra X-Api-Key
                .requestMatchers(HttpMethod.POST, "/device/heartbeat", "/event/logEvent").permitAll()
                // READ (admin)
                .requestMatchers(HttpMethod.GET, "/device/list", "/event/listEvents").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(deviceApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
