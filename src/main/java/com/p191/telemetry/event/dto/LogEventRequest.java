package com.p191.telemetry.event.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

/**
 * Payload cho POST /event/logEvent — app khách bắn mỗi khi xử lý xong 1 tin nhắn.
 * hasError mặc định false nếu client không gửi (Boolean để phân biệt "không gửi").
 */
public record LogEventRequest(
        @NotBlank String deviceId,
        String category,
        Long latencyMs,
        Boolean hasError,
        String errorMessage,
        Instant timestamp
) {}
