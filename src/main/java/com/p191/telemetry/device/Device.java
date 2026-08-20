package com.p191.telemetry.device;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Một máy khách (thiết bị chạy app tài xế). Định danh nghiệp vụ là deviceId do client gửi lên.
 * Bản ghi được tạo/ cập nhật mỗi lần nhận heartbeat.
 */
@Entity
@Table(name = "device", indexes = @Index(name = "ux_device_device_id", columnList = "deviceId", unique = true))
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deviceId;

    private String model;

    private String appVersion;

    private Instant firstSeen;

    private Instant lastSeen;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }

    public Instant getFirstSeen() { return firstSeen; }
    public void setFirstSeen(Instant firstSeen) { this.firstSeen = firstSeen; }

    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }
}
