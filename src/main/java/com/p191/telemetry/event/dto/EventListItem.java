package com.p191.telemetry.event.dto;

import java.time.Instant;

public record EventListItem(
        Long id,
        String deviceId,
        String category,
        Long latencyMs,
        boolean hasError,
        String errorMessage,
        Instant occurredAt,
        Instant createdAt
) {}
