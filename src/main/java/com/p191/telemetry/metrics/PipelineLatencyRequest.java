package com.p191.telemetry.metrics;

import java.time.Instant;

public record PipelineLatencyRequest(
        String deviceId, String driverId, String appVersion, Instant timestamp,
        Integer sttMs, Integer classifyMs, Integer summarizeMs, Integer ttsMs) {}
