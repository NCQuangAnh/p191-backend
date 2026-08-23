package com.p191.telemetry.metrics;

import jakarta.persistence.*;
import java.time.Instant;

// Do tre 4 buoc pipeline (STT -> Phan loai -> Tom tat -> TTS) - moi khong
// he co truoc day, backend chi co /event/logEvent (1 latency chung, khong
// tach buoc, va endpoint do KHONG duoc app dung - xem TelemetryIngestController).
@Entity
@Table(name = "pipeline_latency_events", indexes = @Index(name = "ix_pipeline_latency_received", columnList = "received_at"))
public class PipelineLatencyEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(name = "device_id", nullable = false, length = 64) private String deviceId;
    @Column(name = "driver_id", length = 64) private String driverId;
    @Column(name = "app_version", length = 32) private String appVersion;
    @Column(name = "stt_ms") private Integer sttMs;
    @Column(name = "classify_ms") private Integer classifyMs;
    @Column(name = "summarize_ms") private Integer summarizeMs;
    @Column(name = "tts_ms") private Integer ttsMs;
    @Column(name = "received_at", nullable = false, updatable = false) private Instant receivedAt = Instant.now();

    protected PipelineLatencyEvent() {}

    public static PipelineLatencyEvent from(PipelineLatencyRequest r) {
        PipelineLatencyEvent e = new PipelineLatencyEvent();
        e.deviceId = r.deviceId(); e.driverId = r.driverId(); e.appVersion = r.appVersion();
        e.sttMs = r.sttMs(); e.classifyMs = r.classifyMs(); e.summarizeMs = r.summarizeMs(); e.ttsMs = r.ttsMs();
        return e;
    }
}
