package com.p191.telemetry.event;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Một sự kiện xử lý xong 1 tin nhắn: category, latency, có lỗi hay không.
 * deviceId lưu dạng chuỗi (không FK cứng) để ghi nhanh và không chặn nếu device chưa heartbeat.
 */
@Entity
@Table(name = "event_log", indexes = {
        @Index(name = "ix_event_device_id", columnList = "deviceId"),
        @Index(name = "ix_event_occurred_at", columnList = "occurredAt")
})
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceId;

    private String category;

    private Long latencyMs;

    @Column(nullable = false)
    private boolean hasError;

    @Column(length = 2000)
    private String errorMessage;

    /** Thời điểm sự kiện xảy ra (client gửi lên, hoặc = createdAt nếu thiếu). */
    private Instant occurredAt;

    /** Thời điểm backend nhận và ghi. */
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }

    public boolean isHasError() { return hasError; }
    public void setHasError(boolean hasError) { this.hasError = hasError; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
