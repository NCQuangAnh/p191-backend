package com.p191.telemetry.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ButtonEventRepository extends JpaRepository<ButtonEvent, Long> {
    // tổng lần bấm theo từng buttonCode → dashboard xếp nút 4 (SOS) lên đầu
    @Query("select b.buttonCode as code, sum(b.pressCount) as presses " +
            "from ButtonEvent b group by b.buttonCode order by b.buttonCode")
    List<ButtonPressAgg> aggregatePresses();

    interface ButtonPressAgg { Integer getCode(); Long getPresses(); }
}