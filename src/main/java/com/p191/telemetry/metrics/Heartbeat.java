package com.p191.telemetry.metrics;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "heartbeats", indexes = {
        @Index(name = "ix_hb_device",   columnList = "device_id"),
        @Index(name = "ix_hb_received", columnList = "received_at")
})
public class Heartbeat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, length = 64) private String deviceId;
    @Column(name = "driver_id", length = 64)   private String driverId;
    @Column(name = "app_version", length = 32) private String appVersion;
    @Column(name = "client_time")              private Instant clientTime;   // giờ app gửi
    @Column(name = "ble_connected")            private Boolean bleConnected;
    @Column(name = "ble_device_id", length = 128) private String bleDeviceId;
    @Column(name = "driver_enabled")           private Boolean driverEnabled;
    @Column(name = "active_now")               private Boolean activeNow;
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt = Instant.now();                              // giờ BE nhận

    protected Heartbeat() {}

    public static Heartbeat from(HeartbeatRequest r) {
        Heartbeat h = new Heartbeat();
        h.deviceId = r.deviceId(); h.driverId = r.driverId(); h.appVersion = r.appVersion();
        h.clientTime = r.timestamp(); h.bleConnected = r.bleConnected(); h.bleDeviceId = r.bleDeviceId();
        h.driverEnabled = r.driverEnabled(); h.activeNow = r.activeNow();
        return h;
    }
}