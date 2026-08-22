package com.p191.telemetry.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ErrorEventRepository extends JpaRepository<ErrorEvent, Long> {
    @Query("select e.errorType as type, sum(e.count) as total, avg(e.latencyMs) as avgLatency " +
            "from ErrorEvent e group by e.errorType order by total desc")
    List<ErrorAgg> aggregate();

    interface ErrorAgg { String getType(); Long getTotal(); Double getAvgLatency(); }
}