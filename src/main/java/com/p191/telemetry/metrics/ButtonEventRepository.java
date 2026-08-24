package com.p191.telemetry.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface ButtonEventRepository extends JpaRepository<ButtonEvent, Long> {
    // tổng lần bấm theo từng buttonCode → dashboard xếp nút 4 (SOS) lên đầu
    @Query("select b.buttonCode as code, sum(b.pressCount) as presses " +
            "from ButtonEvent b group by b.buttonCode order by b.buttonCode")
    List<ButtonPressAgg> aggregatePresses();

    // Ban co "since" - Dashboard web bo chon Hom nay/7 ngay/30 ngay (yeu cau
    // nguoi dung 24/08). KHONG dung "(:since is null or ...)" - Postgres
    // khong suy duoc kieu tham so $1 luc prepare statement (loi thuc te
    // 24/08: "could not determine data type of parameter $1", 42P18).
    // Controller tu re nhanh goi aggregatePresses() khi since=null.
    @Query("select b.buttonCode as code, sum(b.pressCount) as presses " +
            "from ButtonEvent b where b.receivedAt >= :since group by b.buttonCode order by b.buttonCode")
    List<ButtonPressAgg> aggregatePressesSince(@Param("since") Instant since);

    // Tong luot bam theo TUNG may (deviceId) + tung nut - de tinh trung binh
    // moi user dung firmware bao nhieu lan/ngay, nut nao huu ich (yeu cau
    // nguoi dung 22/08: "Thống kê số lần bấm nút 2, 3, 4 của từng user").
    @Query("select b.deviceId as deviceId, b.buttonCode as code, sum(b.pressCount) as presses " +
            "from ButtonEvent b group by b.deviceId, b.buttonCode order by b.deviceId, b.buttonCode")
    List<ButtonPressByDeviceAgg> aggregatePressesByDevice();

    // Tong luot bam theo TUNG NGAY + tung nut, N ngay gan nhat - de ve bieu
    // do cot theo ngay va tinh % chenh lech so voi hom qua (yeu cau nguoi
    // dung 22/08 - man "Lượt Bấm" kieu bar chart theo ngay).
    @Query(value = "select date(received_at) as day, button_code as code, sum(press_count) as presses " +
            "from button_events where received_at >= :since group by date(received_at), button_code order by day, code",
            nativeQuery = true)
    List<ButtonPressDailyAgg> aggregatePressesDaily(@Param("since") Instant since);

    interface ButtonPressAgg { Integer getCode(); Long getPresses(); }
    interface ButtonPressByDeviceAgg { String getDeviceId(); Integer getCode(); Long getPresses(); }
    interface ButtonPressDailyAgg { java.sql.Date getDay(); Integer getCode(); Long getPresses(); }
}