package com.p191.telemetry.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<EventLog, Long> {

    /** Lọc tùy chọn theo deviceId / category / hasError; param null nghĩa là bỏ qua điều kiện đó. */
    @Query("""
            SELECT e FROM EventLog e
            WHERE (:deviceId IS NULL OR e.deviceId = :deviceId)
              AND (:category IS NULL OR e.category = :category)
              AND (:hasError IS NULL OR e.hasError = :hasError)
            """)
    Page<EventLog> search(@Param("deviceId") String deviceId,
                          @Param("category") String category,
                          @Param("hasError") Boolean hasError,
                          Pageable pageable);
}
