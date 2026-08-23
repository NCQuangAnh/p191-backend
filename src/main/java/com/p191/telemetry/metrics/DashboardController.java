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
    private final TripRepository trips;
    private final PipelineLatencyRepository pipelineLatency;

    public DashboardController(HeartbeatRepository heartbeats, TripReadService tripRead,
                               SosEventRepository sosEvents, ButtonEventRepository buttons, ErrorEventRepository errors,
                               MessageClassificationRepository classifications, TripRepository trips,
                               PipelineLatencyRepository pipelineLatency) {
        this.heartbeats = heartbeats; this.tripRead = tripRead; this.sosEvents = sosEvents;
        this.buttons = buttons;
        this.errors = errors;
        this.classifications = classifications;
        this.trips = trips;
        this.pipelineLatency = pipelineLatency;
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

    // 7 ngay gan nhat - ve bieu do cot + tinh % so voi hom qua (yeu cau
    // nguoi dung 22/08).
    @GetMapping("/stats/buttons/daily")
    public List<ButtonEventRepository.ButtonPressDailyAgg> buttonStatsDaily() {
        Instant since = Instant.now().minusSeconds(7L * 24 * 3600);
        return buttons.aggregatePressesDaily(since);
    }

    @GetMapping("/stats/errors")
    public List<ErrorEventRepository.ErrorAgg> errorStats() { return errors.aggregate(); }

    @GetMapping("/stats/categories")
    public List<MessageClassificationRepository.CategoryAgg> categoryStats() {
        return classifications.categoryDistribution();
    }

    // "Luu luong tin nhan 24h" - so tin + so chuyen theo tung gio, gop tu 2
    // nguon that (message_classifications + trips), khong con mock (yeu cau
    // nguoi dung 23/08).
    @GetMapping("/stats/hourly")
    public Map<String, Object> hourlyStats() {
        Instant since = Instant.now().minusSeconds(24 * 3600);
        return Map.of(
                "messagesByHour", classifications.messagesPerHour(since),
                "tripsByHour", trips.tripsPerHour(since)
        );
    }

    // Pheu phan hoi tin nhan TOAN FLEET (khong phai 1 trip rieng le) - cong
    // don sum(TripView fields) tren toan bo trips + breakdown theo channel
    // (yeu cau nguoi dung 23/08, thay the du lieu mock tripBehavior).
    @GetMapping("/stats/funnel")
    public Map<String, Object> funnelStats() {
        return Map.of(
                "summary", trips.fleetFunnel(),
                "channels", trips.fleetChannelStats()
        );
    }

    // Do tre trung binh 4 buoc pipeline (STT/Phan loai/Tom tat/TTS) - chi co
    // du lieu neu app da gui qua POST /api/telemetry/pipeline-latency (yeu
    // cau nguoi dung 23/08, thay the du lieu mock aiQuality).
    @GetMapping("/stats/pipeline-latency")
    public Map<String, Object> pipelineLatencyStats() {
        return Map.of(
                "latency", pipelineLatency.averageLatency(),
                "summaryQuality", trips.summaryQuality()
        );
    }
}