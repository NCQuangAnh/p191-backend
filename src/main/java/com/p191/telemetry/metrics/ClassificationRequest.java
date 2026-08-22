package com.p191.telemetry.metrics;

import java.time.Instant;

public record ClassificationRequest(
        String deviceId, String driverId, String appVersion, Instant timestamp,
        String category,       // 1 trong 7: khan_cap/gia_dinh/cong_viec/tin_rac/ban_be/thong_bao_he_thong/khac
        Boolean isImportant    // model nhị phân riêng, độc lập category
) {}