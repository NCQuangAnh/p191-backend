package com.p191.telemetry.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, String> {   // khoá = tripId (String)
    List<Trip> findTop50ByOrderByReceivedAtDesc();

    // Phễu phản hồi TOÀN FLEET (khong phai 1 trip rieng le) - cong don tren
    // toan bo bang trips (yeu cau nguoi dung 23/08).
    @Query("select sum(t.totalIncomingMessages) as totalIncoming, sum(t.declinedListenCount) as declinedListen, " +
            "sum(t.repliedCount) as replied, sum(t.suggestionUsedCount) as suggestionUsed, " +
            "sum(t.composedCount) as composed, sum(t.suggestionEmptyCount) as suggestionEmpty, " +
            "sum(t.clarifyRetryCount) as clarifyRetry, avg(t.avgProcessingMs) as avgProcessingMs " +
            "from Trip t")
    FunnelAgg fleetFunnel();

    // Breakdown theo tung channel (Zalo/SMS/Messenger) - cong don tat ca trip.
    @Query("select c.channel as channel, sum(c.totalIncomingMessages) as total, " +
            "sum(c.suggestionUsedCount) as used, sum(c.composedCount) as composed, " +
            "sum(c.suggestionEmptyCount) as empty from TripChannelStat c group by c.channel")
    List<ChannelAgg> fleetChannelStats();

    // Tom tat AI thanh cong/fallback/bi guardrail chan, cong don toan fleet -
    // ghep voi PipelineLatencyRepository o endpoint /stats/pipeline-latency
    // de khop du model AiPipelineQualityMetrics ben Flutter.
    @Query("select sum(t.summarySuccessCount) as summarySuccess, sum(t.summaryFallbackCount) as summaryFallback, " +
            "sum(t.summaryGuardrailBlockedCount) as summaryGuardrailBlocked from Trip t")
    SummaryQualityAgg summaryQuality();

    // So chuyen bat dau theo tung gio trong ngay, N gio gan nhat. AT TIME
    // ZONE 'Asia/Ho_Chi_Minh' - started_at luu UTC, doi ve gio VN truoc khi
    // extract (xem ghi chu tai MessageClassificationRepository.messagesPerHour,
    // cung 1 bug lech 7 tieng, sua chung dot 24/08).
    @Query(value = "select extract(hour from started_at AT TIME ZONE 'Asia/Ho_Chi_Minh') as hour, count(*) as cnt " +
            "from trips where started_at >= :since " +
            "group by extract(hour from started_at AT TIME ZONE 'Asia/Ho_Chi_Minh')",
            nativeQuery = true)
    List<HourCountAgg> tripsPerHour(@Param("since") Instant since);

    interface FunnelAgg {
        Long getTotalIncoming(); Long getDeclinedListen(); Long getReplied();
        Long getSuggestionUsed(); Long getComposed(); Long getSuggestionEmpty();
        Long getClarifyRetry(); Double getAvgProcessingMs();
    }
    interface ChannelAgg { String getChannel(); Long getTotal(); Long getUsed(); Long getComposed(); Long getEmpty(); }
    interface SummaryQualityAgg { Long getSummarySuccess(); Long getSummaryFallback(); Long getSummaryGuardrailBlocked(); }
    interface HourCountAgg { Number getHour(); Long getCnt(); }
}