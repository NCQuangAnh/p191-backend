package com.p191.telemetry.audit;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "ix_audit_actor",   columnList = "actor"),
        @Index(name = "ix_audit_created", columnList = "created_at")
})
public class AuditEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String actor;            // username, hoặc "ANONYMOUS"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AuditAction action;

    @Column(length = 100)  private String target;   // đối tượng bị tác động
    @Column(length = 500)  private String detail;
    @Column(length = 45)   private String ip;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AuditEvent() {}

    public AuditEvent(String actor, AuditAction action, String target, String detail, String ip) {
        this.actor = actor; this.action = action; this.target = target;
        this.detail = detail; this.ip = ip;
    }

    public Long getId() { return id; }
    public String getActor() { return actor; }
    public AuditAction getAction() { return action; }
    public String getTarget() { return target; }
    public String getDetail() { return detail; }
    public String getIp() { return ip; }
    public Instant getCreatedAt() { return createdAt; }
}