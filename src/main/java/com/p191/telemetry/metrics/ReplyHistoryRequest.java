package com.p191.telemetry.metrics;

import java.time.Instant;

public record ReplyHistoryRequest(
        String deviceId, String deviceModel, String driverId, String appVersion, Instant timestamp,
        String replyType, Integer totalMs) {}
