package com.p191.telemetry.metrics;

import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final HeartbeatRepository heartbeats;
    private final TripReadService tripRead;
    private final SosEventRepository sosEvents;
    private final ButtonEventRepository buttons;
    private final ErrorEventRepository errors;
    private final MessageClassificationRepository classifications;

    public DashboardController(HeartbeatRepository heartbeats, TripReadService tripRead,
                               SosEventRepository sosEvents, ButtonEventRepository buttons, ErrorEventRepository errors, MessageClassificationRepository classifications) {
        this.heartbeats = heartbeats; this.tripRead = tripRead; this.sosEvents = sosEvents;
        this.buttons = buttons;
        this.errors = errors;
        this.classifications = classifications;
    }

    @GetMapping("/active-now")
    public Map<String, Object> activeNow() {
        Instant since = Instant.now().minusSeconds(5 * 60);
        return Map.of("activeNow", heartbeats.countActiveDevicesSince(since), "windowMinutes", 5);
    }

    @GetMapping("/trips")
    public List<TripView> trips() { return tripRead.recentTrips(); }

    @GetMapping("/sos")
    public Map<String, Object> sos() {
        Instant since = Instant.now().minusSeconds(24 * 60 * 60);
        return Map.of(
                "last24h", sosEvents.countByReceivedAtGreaterThanEqual(since),   // badge đỏ
                "events",  sosEvents.findTop100ByOrderByReceivedAtDesc()          // danh sách, mới nhất trước
        );
    }

    @GetMapping("/stats/buttons")
    public List<ButtonEventRepository.ButtonPressAgg> buttonStats() { return buttons.aggregatePresses(); }

    // Tach theo tung may (deviceId) - de tinh trung binh moi user dung nut
    // nao bao nhieu lan/ngay (yeu cau nguoi dung 22/08).
    @GetMapping("/stats/buttons/by-device")
    public List<ButtonEventRepository.ButtonPressByDeviceAgg> buttonStatsByDevice() {
        return buttons.aggregatePressesByDevice();
    }

    @GetMapping("/stats/errors")
    public List<ErrorEventRepository.ErrorAgg> errorStats() { return errors.aggregate(); }

    @GetMapping("/stats/categories")
    public List<MessageClassificationRepository.CategoryAgg> categoryStats() {
        return classifications.categoryDistribution();
    }
}