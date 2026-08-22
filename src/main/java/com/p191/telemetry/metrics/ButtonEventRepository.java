package com.p191.telemetry.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ButtonEventRepository extends JpaRepository<ButtonEvent, Long> {
    // tổng lần bấm theo từng buttonCode → dashboard xếp nút 4 (SOS) lên đầu
    @Query("select b.buttonCode as code, sum(b.pressCount) as presses " +
            "from ButtonEvent b group by b.buttonCode order by b.buttonCode")
    List<ButtonPressAgg> aggregatePresses();

    // Tong luot bam theo TUNG may (deviceId) + tung nut - de tinh trung binh
    // moi user dung firmware bao nhieu lan/ngay, nut nao huu ich (yeu cau
    // nguoi dung 22/08: "Thống kê số lần bấm nút 2, 3, 4 của từng user").
    @Query("select b.deviceId as deviceId, b.buttonCode as code, sum(b.pressCount) as presses " +
            "from ButtonEvent b group by b.deviceId, b.buttonCode order by b.deviceId, b.buttonCode")
    List<ButtonPressByDeviceAgg> aggregatePressesByDevice();

    interface ButtonPressAgg { Integer getCode(); Long getPresses(); }
    interface ButtonPressByDeviceAgg { String getDeviceId(); Integer getCode(); Long getPresses(); }
}