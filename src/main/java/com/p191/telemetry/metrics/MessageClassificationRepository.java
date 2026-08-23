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

    // So tin nhan theo tung gio trong ngay, N gio gan nhat - ve bieu do
    // "Luu luong tin nhan 24h" (yeu cau nguoi dung 23/08).
    @Query(value = "select extract(hour from received_at) as hour, count(*) as cnt " +
            "from message_classifications where received_at >= :since group by extract(hour from received_at)",
            nativeQuery = true)
    List<HourCountAgg> messagesPerHour(@Param("since") Instant since);

    interface CategoryAgg { String getCategory(); Long getTotal(); Long getImportantCount(); }
    interface HourCountAgg { Number getHour(); Long getCnt(); }
}