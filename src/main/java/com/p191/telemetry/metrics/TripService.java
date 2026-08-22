package com.p191.telemetry.metrics;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TripService {
    private final TripRepository trips;
    public TripService(TripRepository trips) { this.trips = trips; }

    private static int nz(Integer v) { return v == null ? 0 : v; }   // null → 0

    @Transactional
    public void ingest(TripRequest r) {
        // upsert theo tripId
        Trip t = trips.findById(r.tripId()).orElseGet(() -> new Trip(r.tripId()));

        t.setDeviceId(r.deviceId()); t.setDriverId(r.driverId()); t.setAppVersion(r.appVersion());
        t.setStartedAt(r.startedAt()); t.setEndedAt(r.endedAt()); t.setDurationMinutes(r.durationMinutes());

        int replied = nz(r.repliedCount());
        int used = nz(r.suggestionUsedCount()), composed = nz(r.composedCount()), empty = nz(r.suggestionEmptyCount());
        t.setTotalIncomingMessages(nz(r.totalIncomingMessages()));
        t.setDeclinedListenCount(nz(r.declinedListenCount()));
        t.setRepliedCount(replied);
        t.setSuggestionUsedCount(used); t.setComposedCount(composed); t.setSuggestionEmptyCount(empty);
        t.setSummarySuccessCount(nz(r.summarySuccessCount()));
        t.setSummaryFallbackCount(nz(r.summaryFallbackCount()));
        t.setSummaryGuardrailBlockedCount(nz(r.summaryGuardrailBlockedCount()));
        t.setClarifyRetryCount(nz(r.clarifyRetryCount()));
        t.setAvgProcessingMs(r.avgProcessingMs());
        t.setCountsConsistent(replied == used + composed + empty);   // cross-check §2
        t.setReceivedAt(java.time.Instant.now());

        // §8.4: app gửi total running → thay nguyên danh sách channel
        t.getChannels().clear();
        if (r.channels() != null)
            for (ChannelStatRequest c : r.channels())
                t.addChannel(new TripChannelStat(c.channel(), nz(c.totalIncomingMessages()),
                        nz(c.suggestionUsedCount()), nz(c.composedCount()), nz(c.suggestionEmptyCount())));

        trips.save(t);
    }
}