package com.p191.telemetry.metrics;

import java.time.Instant;

public record ErrorEventRequest(
        String deviceId, String driverId, String appVersion, Instant timestamp,
        String errorType,    // stt_timeout, gps_timeout, tts_fail, llm_timeout...
        Integer count,
        Integer latencyMs
) {}