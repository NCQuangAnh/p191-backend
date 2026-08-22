package com.p191.telemetry.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface SosEventRepository extends JpaRepository<SosEvent, Long> {
    List<SosEvent> findTop100ByOrderByReceivedAtDesc();       // dashboard: mới nhất trước
    long countByReceivedAtGreaterThanEqual(Instant since);    // badge "SOS trong 24h"
}