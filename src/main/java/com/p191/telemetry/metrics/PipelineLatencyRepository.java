package com.p191.telemetry.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PipelineLatencyRepository extends JpaRepository<PipelineLatencyEvent, Long> {
    @Query("select avg(e.sttMs) as sttMs, avg(e.classifyMs) as classifyMs, " +
            "avg(e.summarizeMs) as summarizeMs, avg(e.ttsMs) as ttsMs, count(e) as sampleCount " +
            "from PipelineLatencyEvent e")
    List<PipelineLatencyAgg> averageLatency();

    interface PipelineLatencyAgg {
        Double getSttMs(); Double getClassifyMs(); Double getSummarizeMs(); Double getTtsMs(); Long getSampleCount();
    }
}
