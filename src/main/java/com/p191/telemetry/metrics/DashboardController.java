package com.p191.telemetry.metrics;

import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    /**
     * Mui gio nghiep vu cua du an. Phai KHOP voi mui gio hard-code trong
     * MessageClassificationRepository.messagesPerHour va
     * TripRepository.tripsPerHour - lech nhau thi moc "dau ngay" khong trung
     * voi cach 2 query xep gio, bieu do se le mot vai khung.
     */
    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    private final HeartbeatRepository heartbeats;
    private final TripReadService tripRead;
    private final SosEventRepository sosEvents;
    private final ButtonEventRepository buttons;
    private final ErrorEventRepository errors;
    private final MessageClassificationRepository classifications;
    private final TripRepository trips;
    private final PipelineLatencyRepository pipelineLatency;
    private final ReplyHistoryRepository replyHistory;

    public DashboardController(HeartbeatRepository heartbeats, TripReadService tripRead,
                               SosEventRepository sosEvents, ButtonEventRepository buttons, ErrorEventRepository errors,
                               MessageClassificationRepository classifications, TripRepository trips,
                               PipelineLatencyRepository pipelineLatency, ReplyHistoryRepository replyHistory) {
        this.heartbeats = heartbeats; this.tripRead = tripRead; this.sosEvents = sosEvents;
        this.buttons = buttons;
        this.errors = errors;
        this.classifications = classifications;
        this.trips = trips;
        this.pipelineLatency = pipelineLatency;
        this.replyHistory = replyHistory;
    }

    @GetMapping("/active-now")
    public Map<String, Object> activeNow() {
        Instant since = Instant.now().minusSeconds(5 * 60);
        return Map.of("activeNow", heartbeats.countActiveDevicesSince(since), "windowMinutes", 5);
    }

    // days=null -> toan bo thoi gian (giu nguyen hanh vi cu). Dashboard web
    // dung cho bo chon Hom nay(1)/7 ngay/30 ngay (yeu cau nguoi dung 24/08).
    private static Instant sinceDays(Integer days) {
        return days == null ? null : Instant.now().minusSeconds(days * 24L * 3600);
    }

    @GetMapping("/trips")
    public List<TripView> trips() { return tripRead.recentTrips(); }

    // So chuyen (khong gioi han Top50) trong khung thoi gian - the "Chuyến
    // đi hợp lệ" tren Dashboard web (yeu cau nguoi dung 24/08).
    @GetMapping("/stats/trip-count")
    public Map<String, Object> tripCount(@RequestParam(required = false) Integer days) {
        Instant since = sinceDays(days);
        return Map.of("count", since == null ? trips.count() : trips.countSince(since));
    }

    @GetMapping("/sos")
    public Map<String, Object> sos() {
        Instant since = Instant.now().minusSeconds(24 * 60 * 60);
        return Map.of(
                "last24h", sosEvents.countByReceivedAtGreaterThanEqual(since),   // badge đỏ
                "events",  sosEvents.findTop100ByOrderByReceivedAtDesc()          // danh sách, mới nhất trước
        );
    }

    @GetMapping("/stats/buttons")
    public List<ButtonEventRepository.ButtonPressAgg> buttonStats(@RequestParam(required = false) Integer days) {
        Instant since = sinceDays(days);
        return since == null ? buttons.aggregatePresses() : buttons.aggregatePressesSince(since);
    }

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
    public List<MessageClassificationRepository.CategoryAgg> categoryStats(@RequestParam(required = false) Integer days) {
        Instant since = sinceDays(days);
        return since == null ? classifications.categoryDistribution() : classifications.categoryDistributionSince(since);
    }

    // Danh sach tho de man "Chi tiết nhãn tin nhắn" loc theo ngay + ve pie
    // chart (yeu cau nguoi dung 23/08).
    @GetMapping("/stats/categories/raw")
    public List<MessageClassification> categoryStatsRaw() {
        return classifications.findTop500ByOrderByReceivedAtDesc();
    }

    // "Luu luong tin nhan 24h" - so tin + so chuyen theo tung gio, gop tu 2
    // nguon that (message_classifications + trips), khong con mock (yeu cau
    // nguoi dung 23/08).
    //
    // Moc bat dau la 00:00 HOM NAY theo gio VN, KHONG phai "24 gio gan nhat"
    // (Instant.now().minusSeconds(24*3600)) nhu truoc. Ly do: 2 query ben
    // duoi gom theo GIO TRONG NGAY (extract(hour ...)), nen cua so truot
    // khien cung 1 khung gio chua du lieu cua CA HAI ngay - vd luc 00:50 thi
    // khung "0-2h" cong ca 00:50-02:00 hom qua lan 00:00-00:50 hom nay. Nhin
    // tren bieu do giong nhu du lieu khong bao gio bi xoa di (nguoi dung bao
    // "ghi 24h nhung thuc ra dang luu all time", 26/08).
    //
    // Dung ZONE_VN cho khop voi chinh mui gio ma 2 query dung khi doi
    // received_at/started_at sang gio dia phuong.
    @GetMapping("/stats/hourly")
    public Map<String, Object> hourlyStats() {
        Instant since = LocalDate.now(ZONE_VN).atStartOfDay(ZONE_VN).toInstant();
        return Map.of(
                "messagesByHour", classifications.messagesPerHour(since),
                "tripsByHour", trips.tripsPerHour(since)
        );
    }

    // Pheu phan hoi tin nhan TOAN FLEET (khong phai 1 trip rieng le) - cong
    // don sum(TripView fields) tren toan bo trips + breakdown theo channel
    // (yeu cau nguoi dung 23/08, thay the du lieu mock tripBehavior).
    @GetMapping("/stats/funnel")
    public Map<String, Object> funnelStats(@RequestParam(required = false) Integer days) {
        Instant since = sinceDays(days);
        return Map.of(
                "summary", since == null ? trips.fleetFunnel() : trips.fleetFunnelSince(since),
                "channels", trips.fleetChannelStats()
        );
    }

    // Do tre trung binh 4 buoc pipeline (STT/Phan loai/Tom tat/TTS) - chi co
    // du lieu neu app da gui qua POST /api/telemetry/pipeline-latency (yeu
    // cau nguoi dung 23/08, thay the du lieu mock aiQuality).
    @GetMapping("/stats/pipeline-latency")
    public Map<String, Object> pipelineLatencyStats(@RequestParam(required = false) Integer days) {
        Instant since = sinceDays(days);
        return Map.of(
                "latency", since == null ? pipelineLatency.averageLatency() : pipelineLatency.averageLatencySince(since),
                "summaryQuality", since == null ? trips.summaryQuality() : trips.summaryQualitySince(since)
        );
    }

    // Do tre trung binh "Tu soan" vs "Goi y tra loi" - man "Lich su" ben
    // admin (yeu cau nguoi dung 23/08).
    @GetMapping("/stats/reply-history/summary")
    public List<ReplyHistoryRepository.ReplyTypeAvgAgg> replyHistorySummary() {
        return replyHistory.averageByType();
    }

    @GetMapping("/stats/reply-history/detail")
    public List<ReplyHistoryEvent> replyHistoryDetail() {
        return replyHistory.findTop200ByOrderByReceivedAtDesc();
    }
}