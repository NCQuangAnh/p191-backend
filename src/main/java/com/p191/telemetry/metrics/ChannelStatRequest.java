package com.p191.telemetry.metrics;

public record ChannelStatRequest(
        String channel,
        Integer totalIncomingMessages,
        Integer suggestionUsedCount,
        Integer composedCount,
        Integer suggestionEmptyCount
) {}