package com.p191.telemetry.device.dto;

import java.time.Instant;

/**
 * Item trả về cho admin ở GET /device/list.
 * online = true nếu lastSeen còn trong ngưỡng app.device.online-threshold-seconds.
 */
public record DeviceListItem(
        String deviceId,
        String model,
        String appVersion,
        Instant firstSeen,
        Instant lastSeen,
        boolean online
) {}
