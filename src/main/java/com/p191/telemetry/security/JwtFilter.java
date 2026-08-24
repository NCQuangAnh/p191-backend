package com.p191.telemetry.security;

import com.p191.telemetry.config.JwtService;
import com.p191.telemetry.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository users;

    public JwtFilter(JwtService jwtService, UserDetailsService userDetailsService, UserRepository users) {
        this.jwtService = jwtService; this.userDetailsService = userDetailsService; this.users = users;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (request.getServletPath().startsWith("/api/auth")) {   // login/register bỏ qua
            filterChain.doFilter(request, response); return;
        }
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); return;      // device X-Api-Key (không Bearer) → đi tiếp
        }
        String jwt = authHeader.substring(7);
        String username = jwtService.extractUsername(jwt);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                touchLastSeen(username);
            }
        }
        filterChain.doFilter(request, response);
    }

    // Ghi lai lan cuoi thay username nay - "admin dang online" (yeu cau
    // nguoi dung 24/08). Chay tren MOI request co JWT hop le (dashboard poll
    // 10s/lan nen tu nhien "tuoi" lien tuc trong khi admin con mo app/web).
    // Khong loai tru DRIVER de tranh 1 query kiem tra role thua - chi 2
    // repository method (findAllAdmins/countAdminsOnlineSince) loc theo
    // role khi doc, con o day chi can ghi. Khong can @Transactional rieng -
    // JpaRepository.save() da tu commit transaction cua no (goi tu trong
    // cung 1 class se bo qua proxy AOP neu co @Transactional o day).
    protected void touchLastSeen(String username) {
        users.findByUsername(username).ifPresent(u -> {
            u.setLastSeenAt(Instant.now());
            users.save(u);
        });
    }
}