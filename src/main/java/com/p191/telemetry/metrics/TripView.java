package com.p191.telemetry.metrics;

import java.time.Instant;

public record TripView(
        String tripId, String deviceId, Instant startedAt, Instant endedAt,
        Integer durationMinutes, boolean validTrip,        // §8.2: hợp lệ nếu ≥8 phút
        int totalIncomingMessages, int repliedCount,
        int suggestionUsedCount, int composedCount, int suggestionEmptyCount,
        Boolean countsConsistent) {}