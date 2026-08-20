package com.p191.telemetry.device;

import com.p191.telemetry.device.dto.DeviceListItem;
import com.p191.telemetry.device.dto.HeartbeatRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device")
@Tag(name = "Device")
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    /** WRITE — app khách gọi, xác thực bằng X-Api-Key (không cần JWT). */
    @PostMapping("/heartbeat")
    @SecurityRequirement(name = "deviceApiKey")
    public ResponseEntity<Void> heartbeat(@Valid @RequestBody HeartbeatRequest req) {
        service.heartbeat(req);
        return ResponseEntity.noContent().build();
    }

    /** READ — chỉ admin (JWT role ADMIN). */
    @GetMapping("/list")
    @SecurityRequirement(name = "bearerAuth")
    public List<DeviceListItem> list() {
        return service.list();
    }
}
