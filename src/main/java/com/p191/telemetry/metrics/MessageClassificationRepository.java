package com.p191.telemetry.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MessageClassificationRepository extends JpaRepository<MessageClassification, Long> {
    @Query("select c.category as category, count(c) as total, " +
            "sum(case when c.isImportant = true then 1 else 0 end) as importantCount " +
            "from MessageClassification c group by c.category order by total desc")
    List<CategoryAgg> categoryDistribution();

    interface CategoryAgg { String getCategory(); Long getTotal(); Long getImportantCount(); }
}