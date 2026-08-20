package com.p191.telemetry.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Chặn các endpoint GHI của app khách: yêu cầu header X-Api-Key khớp app.security.device-api-key.
 * Đây là cửa "máy có phải app hợp lệ không"; còn "máy nào" thì lấy từ deviceId trong payload.
 */
@Component
public class DeviceApiKeyFilter extends OncePerRequestFilter {

    private static final Set<String> WRITE_PATHS = Set.of("/device/heartbeat", "/event/logEvent");
    private static final String HEADER = "X-Api-Key";

    private final String expectedKey;

    public DeviceApiKeyFilter(@Value("${app.security.device-api-key}") String expectedKey) {
        this.expectedKey = expectedKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (WRITE_PATHS.contains(request.getServletPath())) {
            String key = request.getHeader(HEADER);
            if (key == null || !expectedKey.equals(key)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\":\"Thiếu hoặc sai X-Api-Key\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
