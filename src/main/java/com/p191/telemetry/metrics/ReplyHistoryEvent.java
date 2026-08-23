package com.p191.telemetry.metrics;

import jakarta.persistence.*;
import java.time.Instant;

// 1 ban ghi = 1 lan tra loi tin nhan XU LY THANH CONG (gui duoc tin) -
// KHONG luu neu nguoi dung bo giua chung/gui that bai (yeu cau nguoi dung
// 23/08: "nếu ko xử lý xong thì sẽ ko lưu vào lịch sử"). totalMs tinh tu
// luc tra loi "co" cho cau "ban co muon nghe khong" (hoac tu luc nut 1 tim
// thay tin theo ten nguoi gui) toi luc gui tin xong - xem
// voice_loop_notifier.dart _replyHistoryStopwatch.
@Entity
@Table(name = "reply_history_events", indexes = {
        @Index(name = "ix_reply_history_received", columnList = "received_at"),
        @Index(name = "ix_reply_history_type", columnList = "reply_type")
})
public class ReplyHistoryEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(name = "device_id", nullable = false, length = 64) private String deviceId;
    @Column(name = "device_model", length = 128) private String deviceModel;
    @Column(name = "driver_id", length = 64) private String driverId;
    @Column(name = "app_version", length = 32) private String appVersion;
    @Column(name = "reply_type", nullable = false, length = 16) private String replyType; // "suggestion" | "compose"
    @Column(name = "total_ms", nullable = false) private Integer totalMs;
    @Column(name = "received_at", nullable = false, updatable = false) private Instant receivedAt = Instant.now();

    protected ReplyHistoryEvent() {}

    public static ReplyHistoryEvent from(ReplyHistoryRequest r) {
        ReplyHistoryEvent e = new ReplyHistoryEvent();
        e.deviceId = r.deviceId(); e.deviceModel = r.deviceModel(); e.driverId = r.driverId();
        e.appVersion = r.appVersion(); e.replyType = r.replyType(); e.totalMs = r.totalMs();
        return e;
    }

    public Long getId() { return id; }
    public String getDeviceId() { return deviceId; }
    public String getDeviceModel() { return deviceModel; }
    public String getReplyType() { return replyType; }
    public Integer getTotalMs() { return totalMs; }
    public Instant getReceivedAt() { return receivedAt; }
}
