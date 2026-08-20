package com.p191.telemetry.device.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

/**
 * Payload cho POST /device/heartbeat.
 * timestamp là tùy chọn — nếu client không gửi, backend dùng thời điểm nhận được request.
 */
public record HeartbeatRequest(
        @NotBlank String deviceId,
        String model,
        String appVersion,
        Instant timestamp
) {}
