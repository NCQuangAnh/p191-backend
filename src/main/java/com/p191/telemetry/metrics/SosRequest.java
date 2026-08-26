package com.p191.telemetry.metrics;

import java.time.Instant;

public record SosRequest(
        String deviceId, String driverId, String appVersion, String deviceModel, Instant timestamp,
        Instant triggeredAt,        // §4
        Integer contactsSent,       // SMS gửi thành công (radio-confirmed)
        Integer contactsTotal,      // tổng liên hệ đã cài (≤3)
        Boolean success             // gửi được ít nhất 1 SMS
) {}