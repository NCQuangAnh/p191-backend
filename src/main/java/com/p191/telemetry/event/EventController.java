package com.p191.telemetry.event;

import com.p191.telemetry.event.dto.EventListItem;
import com.p191.telemetry.event.dto.LogEventRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/event")
@Tag(name = "Event")
public class EventController {

    private static final int MAX_PAGE_SIZE = 200;

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    /** WRITE — app khách gọi, xác thực bằng X-Api-Key (không cần JWT). */
    @PostMapping("/logEvent")
    @SecurityRequirement(name = "deviceApiKey")
    public ResponseEntity<Void> logEvent(@Valid @RequestBody LogEventRequest req) {
        service.log(req);
        return ResponseEntity.noContent().build();
    }

    /** READ — chỉ admin. Hỗ trợ lọc theo deviceId/category/hasError và phân trang. */
    @GetMapping("/listEvents")
    @SecurityRequirement(name = "bearerAuth")
    public Page<EventListItem> listEvents(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean hasError,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var pageable = PageRequest.of(Math.max(page, 0), safeSize,
                Sort.by(Sort.Direction.DESC, "occurredAt"));
        return service.list(deviceId, category, hasError, pageable);
    }
}
