package com.p191.telemetry.metrics;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "message_classifications", indexes = @Index(name="ix_cls_cat", columnList="category"))
public class MessageClassification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="device_id", nullable=false, length=64) private String deviceId;
    @Column(length=32) private String category;
    @Column(name="is_important") private Boolean isImportant;
    @Column(name="received_at", nullable=false, updatable=false) private Instant receivedAt = Instant.now();

    protected MessageClassification() {}
    public static MessageClassification from(ClassificationRequest r) {
        MessageClassification c = new MessageClassification();
        c.deviceId=r.deviceId(); c.category=r.category(); c.isImportant=r.isImportant();
        return c;
    }
}