package com.p191.telemetry.metrics;

import java.time.Instant;

public record ButtonEventRequest(
        String deviceId, String driverId, String appVersion, Instant timestamp,
        Integer buttonCode,       // 1..4 (0x01..0x04)
        Boolean success,
        Integer pressCount,       // §8.8: số lần bấm trong kỳ gửi (mặc định 1 nếu null)
        Integer foundCount,       // chỉ nút 3
        Double distanceKm         // chỉ nút 3 — KHÔNG gửi toạ độ chi tiết
) {}