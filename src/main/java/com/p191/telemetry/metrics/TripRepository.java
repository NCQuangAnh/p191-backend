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

    // Ban co "since" - Dashboard web bo chon Hom nay/7 ngay/30 ngay (yeu cau
    // nguoi dung 24/08). KHONG dung "(:since is null or ...)" - Postgres
    // khong suy duoc kieu tham so $1 luc prepare statement (loi thuc te
    // 24/08: "could not determine data type of parameter $1", 42P18).
    // Controller tu re nhanh goi fleetFunnel() khi since=null.
    @Query("select sum(t.totalIncomingMessages) as totalIncoming, sum(t.declinedListenCount) as declinedListen, " +
            "sum(t.repliedCount) as replied, sum(t.suggestionUsedCount) as suggestionUsed, " +
            "sum(t.composedCount) as composed, sum(t.suggestionEmptyCount) as suggestionEmpty, " +
            "sum(t.clarifyRetryCount) as clarifyRetry, avg(t.avgProcessingMs) as avgProcessingMs " +
            "from Trip t where t.receivedAt >= :since")
    FunnelAgg fleetFunnelSince(@Param("since") Instant since);

    // So chuyen (khong gioi han Top50) trong khung thoi gian - dung cho the
    // "Chuyến đi hợp lệ" tren Dashboard web khi loc theo Hom nay/7 ngay/30
    // ngay (yeu cau nguoi dung 24/08). Loc rieng durationMinutes >= 8 NGAY O
    // DAY (yeu cau nguoi dung 28/08) - truoc day dua vao app CHI gui trip
    // >= 8 phut nen khong can loc them, nhung tu khi app gui MOI chuyen
    // (ke ca ngan) de Pheu Phan hoi Tin nhan (fleetFunnel/fleetFunnelSince,
    // KHONG loc duration - cong don toan bo) khong bi mat du lieu, bang
    // "Chuyến đi hợp lệ" phai tu loc lay o day de van chi tinh chuyen du dai.
    @Query("select count(t) from Trip t where t.receivedAt >= :since and t.durationMinutes >= 8")
    long countSince(@Param("since") Instant since);

    // Nhanh khi since=null (xem ghi chu tai countSince() o tren ve ly do
    // khong con dung JpaRepository.count() co san nua - can loc duration).
    @Query("select count(t) from Trip t where t.durationMinutes >= 8")
    long countValid();

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

    // Controller tu re nhanh goi summaryQuality() khi since=null (ly do:
    // xem ghi chu tai countSince()/fleetFunnelSince() o tren).
    @Query("select sum(t.summarySuccessCount) as summarySuccess, sum(t.summaryFallbackCount) as summaryFallback, " +
            "sum(t.summaryGuardrailBlockedCount) as summaryGuardrailBlocked from Trip t where t.receivedAt >= :since")
    SummaryQualityAgg summaryQualitySince(@Param("since") Instant since);

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