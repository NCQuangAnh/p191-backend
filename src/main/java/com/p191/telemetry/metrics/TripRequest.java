package com.p191.telemetry.metrics;

import java.time.Instant;
import java.util.List;

public record TripRequest(
        // §1 định danh
        String deviceId, String driverId, String appVersion, Instant timestamp,
        // §8.2 trip
        String tripId, Instant startedAt, Instant endedAt, Integer durationMinutes,
        // §2 counters trả lời tin (tổng của trip)
        Integer totalIncomingMessages, Integer declinedListenCount, Integer repliedCount,
        Integer suggestionUsedCount, Integer composedCount, Integer suggestionEmptyCount,
        // §8.6 tóm tắt
        Integer summarySuccessCount, Integer summaryFallbackCount, Integer summaryGuardrailBlockedCount,
        // §8.10 hỏi lại
        Integer clarifyRetryCount,
        // §8.5 thời gian xử lý (trung bình cả chuyến)
        Integer avgProcessingMs,
        // §8.4 tách theo channel
        List<ChannelStatRequest> channels
) {}