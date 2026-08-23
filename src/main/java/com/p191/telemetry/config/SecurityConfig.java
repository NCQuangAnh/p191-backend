package com.p191.telemetry.config;

import com.p191.telemetry.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtFilter jwtFilter;

    public SecurityConfig(AuthenticationProvider authenticationProvider, JwtFilter jwtFilter) {
        this.authenticationProvider = authenticationProvider;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/telemetry/**").permitAll()
                        .requestMatchers("/api/proxy/**").permitAll()

                        .requestMatchers("/api/admin/users/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/driver/**").hasAnyRole("DRIVER", "ADMIN", "SUPER_ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // FE admin (Flutter Web) deploy tren Vercel, khac origin voi BE Render -
    // browser can header Access-Control-Allow-Origin, khong co bean nay thi
    // .cors(Customizer.withDefaults()) khong tu tao header gi ca, moi request
    // tu web bi browser chan im lang (yeu cau nguoi dung 24/08: deploy FE
    // admin len Vercel, tro ve BE Render nay). Cho phep moi origin (*) vi day
    // la du an hackathon/demo, khong co domain co dinh truoc.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}