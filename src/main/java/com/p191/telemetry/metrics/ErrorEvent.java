package com.p191.telemetry.metrics;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "error_events", indexes = @Index(name="ix_err_type", columnList="error_type"))
public class ErrorEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="device_id", nullable=false, length=64) private String deviceId;
    @Column(name="app_version", length=32) private String appVersion;
    @Column(name="error_type", length=64) private String errorType;
    @Column(name="err_count") private Integer count;      // 'count' là từ nhạy cảm SQL → cột err_count
    @Column(name="latency_ms") private Integer latencyMs;
    @Column(name="received_at", nullable=false, updatable=false) private Instant receivedAt = Instant.now();

    protected ErrorEvent() {}
    public static ErrorEvent from(ErrorEventRequest r) {
        ErrorEvent e = new ErrorEvent();
        e.deviceId=r.deviceId(); e.appVersion=r.appVersion();
        e.errorType=r.errorType(); e.count=r.count(); e.latencyMs=r.latencyMs();
        return e;
    }
}