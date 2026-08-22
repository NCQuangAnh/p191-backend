package com.p191.telemetry.metrics;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "button_events", indexes = {
        @Index(name = "ix_btn_device", columnList = "device_id"),
        @Index(name = "ix_btn_code",   columnList = "button_code")
})
public class ButtonEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(name="device_id", nullable=false, length=64) private String deviceId;
    @Column(name="driver_id", length=64) private String driverId;
    @Column(name="app_version", length=32) private String appVersion;
    @Column(name="button_code") private Integer buttonCode;
    private Boolean success;
    @Column(name="press_count") private Integer pressCount;
    @Column(name="found_count") private Integer foundCount;
    @Column(name="distance_km") private Double distanceKm;
    @Column(name="client_time") private Instant clientTime;
    @Column(name="received_at", nullable=false, updatable=false) private Instant receivedAt = Instant.now();

    protected ButtonEvent() {}
    public static ButtonEvent from(ButtonEventRequest r) {
        ButtonEvent b = new ButtonEvent();
        b.deviceId=r.deviceId(); b.driverId=r.driverId(); b.appVersion=r.appVersion();
        b.buttonCode=r.buttonCode(); b.success=r.success();
        b.pressCount = r.pressCount()!=null ? r.pressCount() : 1;
        b.foundCount=r.foundCount(); b.distanceKm=r.distanceKm(); b.clientTime=r.timestamp();
        return b;
    }
    public Integer getButtonCode(){return buttonCode;} public Integer getPressCount(){return pressCount;}
}