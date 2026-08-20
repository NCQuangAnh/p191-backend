package com.p191.telemetry.event;

import com.p191.telemetry.event.dto.EventListItem;
import com.p191.telemetry.event.dto.LogEventRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class EventService {

    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void log(LogEventRequest req) {
        Instant now = Instant.now();
        EventLog e = new EventLog();
        e.setDeviceId(req.deviceId());
        e.setCategory(req.category());
        e.setLatencyMs(req.latencyMs());
        e.setHasError(Boolean.TRUE.equals(req.hasError()));
        e.setErrorMessage(req.errorMessage());
        e.setOccurredAt(req.timestamp() != null ? req.timestamp() : now);
        e.setCreatedAt(now);
        repository.save(e);
    }

    @Transactional(readOnly = true)
    public Page<EventListItem> list(String deviceId, String category, Boolean hasError, Pageable pageable) {
        return repository.search(emptyToNull(deviceId), emptyToNull(category), hasError, pageable)
                .map(e -> new EventListItem(
                        e.getId(),
                        e.getDeviceId(),
                        e.getCategory(),
                        e.getLatencyMs(),
                        e.isHasError(),
                        e.getErrorMessage(),
                        e.getOccurredAt(),
                        e.getCreatedAt()));
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
