package com.p191.telemetry.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;

public interface HeartbeatRepository extends JpaRepository<Heartbeat, Long> {
    @Query("select count(distinct h.deviceId) from Heartbeat h " +
            "where h.activeNow = true and h.receivedAt >= :since")
    long countActiveDevicesSince(@Param("since") Instant since);
}