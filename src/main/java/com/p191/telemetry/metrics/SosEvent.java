package com.p191.telemetry.metrics;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sos_events", indexes = {
        @Index(name = "ix_sos_received", columnList = "received_at"),
        @Index(name = "ix_sos_device",   columnList = "device_id")
})
public class SosEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, length = 64) private String deviceId;
    @Column(name = "driver_id", length = 64) private String driverId;
    @Column(name = "app_version", length = 32) private String appVersion;

    @Column(name = "triggered_at") private Instant triggeredAt;
    @Column(name = "contacts_sent")  private Integer contactsSent;
    @Column(name = "contacts_total") private Integer contactsTotal;
    private Boolean success;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt = Instant.now();

    protected SosEvent() {}

    public static SosEvent from(SosRequest r) {
        SosEvent s = new SosEvent();
        s.deviceId = r.deviceId(); s.driverId = r.driverId(); s.appVersion = r.appVersion();
        s.triggeredAt = r.triggeredAt() != null ? r.triggeredAt() : r.timestamp();
        s.contactsSent = r.contactsSent(); s.contactsTotal = r.contactsTotal(); s.success = r.success();
        return s;
    }

    public Long getId() { return id; }
    public String getDeviceId() { return deviceId; }
    public String getDriverId() { return driverId; }
    public Instant getTriggeredAt() { return triggeredAt; }
    public Integer getContactsSent() { return contactsSent; }
    public Integer getContactsTotal() { return contactsTotal; }
    public Boolean getSuccess() { return success; }
    public Instant getReceivedAt() { return receivedAt; }
}