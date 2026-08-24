package com.p191.telemetry.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface PipelineLatencyRepository extends JpaRepository<PipelineLatencyEvent, Long> {
    @Query("select avg(e.sttMs) as sttMs, avg(e.classifyMs) as classifyMs, " +
            "avg(e.summarizeMs) as summarizeMs, avg(e.ttsMs) as ttsMs, count(e) as sampleCount " +
            "from PipelineLatencyEvent e")
    List<PipelineLatencyAgg> averageLatency();

    // Ban co "since" - Dashboard web bo chon Hom nay/7 ngay/30 ngay (yeu cau
    // nguoi dung 24/08). since = null = toan bo thoi gian.
    @Query("select avg(e.sttMs) as sttMs, avg(e.classifyMs) as classifyMs, " +
            "avg(e.summarizeMs) as summarizeMs, avg(e.ttsMs) as ttsMs, count(e) as sampleCount " +
            "from PipelineLatencyEvent e where (:since is null or e.receivedAt >= :since)")
    List<PipelineLatencyAgg> averageLatencySince(@Param("since") Instant since);

    interface PipelineLatencyAgg {
        Double getSttMs(); Double getClassifyMs(); Double getSummarizeMs(); Double getTtsMs(); Long getSampleCount();
    }
}
