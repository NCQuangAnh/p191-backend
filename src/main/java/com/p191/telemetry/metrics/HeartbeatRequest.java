package com.p191.telemetry.metrics;

import java.time.Instant;

public record HeartbeatRequest(
        String deviceId,
        String driverId,
        String appVersion,
        Instant timestamp,
        Boolean bleConnected,
        String bleDeviceId,
        Boolean driverEnabled,
        Boolean activeNow
) {}