package com.p191.telemetry.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ReplyHistoryRepository extends JpaRepository<ReplyHistoryEvent, Long> {
    // Do tre trung binh theo loai xu ly ("suggestion" / "compose") - the
    // "Thong ke Tin nhan" (yeu cau nguoi dung 23/08).
    @Query("select e.replyType as replyType, avg(e.totalMs) as avgMs, count(e) as sampleCount " +
            "from ReplyHistoryEvent e group by e.replyType")
    List<ReplyTypeAvgAgg> averageByType();

    // 200 ban ghi gan nhat - man "Xem chi tiet".
    List<ReplyHistoryEvent> findTop200ByOrderByReceivedAtDesc();

    interface ReplyTypeAvgAgg { String getReplyType(); Double getAvgMs(); Long getSampleCount(); }
}
