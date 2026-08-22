package com.p191.telemetry.metrics;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TripReadService {
    private final TripRepository trips;
    public TripReadService(TripRepository trips) { this.trips = trips; }

    @Transactional(readOnly = true)
    public List<TripView> recentTrips() {
        return trips.findTop50ByOrderByReceivedAtDesc().stream().map(this::toView).toList();
    }

    private TripView toView(Trip t) {
        Integer d = t.getDurationMinutes();
        boolean valid = d != null && d >= 8;          // §8.2: hợp lệ nếu ≥8 phút
        return new TripView(
                t.getTripId(), t.getDeviceId(), t.getStartedAt(), t.getEndedAt(),
                d, valid,
                t.getTotalIncomingMessages(), t.getRepliedCount(),
                t.getSuggestionUsedCount(), t.getComposedCount(), t.getSuggestionEmptyCount(),
                t.getCountsConsistent());
    }
}