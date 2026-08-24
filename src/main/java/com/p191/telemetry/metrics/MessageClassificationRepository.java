package com.p191.telemetry.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface MessageClassificationRepository extends JpaRepository<MessageClassification, Long> {
    @Query("select c.category as category, count(c) as total, " +
            "sum(case when c.isImportant = true then 1 else 0 end) as importantCount " +
            "from MessageClassification c group by c.category order by total desc")
    List<CategoryAgg> categoryDistribution();

    // Ban co "since" - loc theo khung thoi gian (Hom nay/7 ngay/30 ngay tren
    // Dashboard web, yeu cau nguoi dung 24/08). since = null nghia la khong
    // loc (toan bo thoi gian, giu nguyen hanh vi cu cho app driver).
    @Query("select c.category as category, count(c) as total, " +
            "sum(case when c.isImportant = true then 1 else 0 end) as importantCount " +
            "from MessageClassification c where (:since is null or c.receivedAt >= :since) group by c.category order by total desc")
    List<CategoryAgg> categoryDistributionSince(@Param("since") Instant since);

    // So tin nhan theo tung gio trong ngay, N gio gan nhat - ve bieu do
    // "Luu luong tin nhan 24h" (yeu cau nguoi dung 23/08). AT TIME ZONE
    // 'Asia/Ho_Chi_Minh' - received_at luu UTC (Instant.now()), khong doi
    // mui gio thi extract(hour) tra ve gio UTC, lech 7 tieng so voi gio VN
    // hien tren app (xac nhan thuc te 24/08: test luc 00h VN, app hien
    // 12h - lech dung 7-8 tieng do bucket 3 gio).
    @Query(value = "select extract(hour from received_at AT TIME ZONE 'Asia/Ho_Chi_Minh') as hour, count(*) as cnt " +
            "from message_classifications where received_at >= :since " +
            "group by extract(hour from received_at AT TIME ZONE 'Asia/Ho_Chi_Minh')",
            nativeQuery = true)
    List<HourCountAgg> messagesPerHour(@Param("since") Instant since);

    // Danh sach tho (category + receivedAt) - man "Chi tiết nhãn tin nhắn"
    // ben admin tu loc theo ngay + ve pie chart client-side (yeu cau nguoi
    // dung 23/08).
    List<MessageClassification> findTop500ByOrderByReceivedAtDesc();

    interface CategoryAgg { String getCategory(); Long getTotal(); Long getImportantCount(); }
    interface HourCountAgg { Number getHour(); Long getCnt(); }
}