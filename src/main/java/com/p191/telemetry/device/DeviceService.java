package com.p191.telemetry.device;

import com.p191.telemetry.device.dto.DeviceListItem;
import com.p191.telemetry.device.dto.HeartbeatRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository repository;
    private final long onlineThresholdSeconds;

    public DeviceService(DeviceRepository repository,
                         @Value("${app.device.online-threshold-seconds}") long onlineThresholdSeconds) {
        this.repository = repository;
        this.onlineThresholdSeconds = onlineThresholdSeconds;
    }

    /** Upsert theo deviceId: tạo mới nếu chưa có, cập nhật model/version/lastSeen nếu đã có. */
    @Transactional
    public void heartbeat(HeartbeatRequest req) {
        Instant now = Instant.now();
        Device device = repository.findByDeviceId(req.deviceId())
                .orElseGet(() -> {
                    Device d = new Device();
                    d.setDeviceId(req.deviceId());
                    d.setFirstSeen(now);
                    return d;
                });
        if (device.getFirstSeen() == null) {
            device.setFirstSeen(now);
        }
        device.setModel(req.model());
        device.setAppVersion(req.appVersion());
        device.setLastSeen(req.timestamp() != null ? req.timestamp() : now);
        repository.save(device);
    }

    @Transactional(readOnly = true)
    public List<DeviceListItem> list() {
        Instant now = Instant.now();
        return repository.findAllByOrderByLastSeenDesc().stream()
                .map(d -> new DeviceListItem(
                        d.getDeviceId(),
                        d.getModel(),
                        d.getAppVersion(),
                        d.getFirstSeen(),
                        d.getLastSeen(),
                        isOnline(d.getLastSeen(), now)))
                .toList();
    }

    private boolean isOnline(Instant lastSeen, Instant now) {
        if (lastSeen == null) return false;
        return Duration.between(lastSeen, now).getSeconds() <= onlineThresholdSeconds;
    }
}
