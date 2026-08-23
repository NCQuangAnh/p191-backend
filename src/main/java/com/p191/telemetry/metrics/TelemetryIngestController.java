package com.p191.telemetry.metrics;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryIngestController {

    private final HeartbeatRepository heartbeats;
    private final TripService tripService;
    private final SosEventRepository sosEvents;
    private final ButtonEventRepository buttons;
    private final ErrorEventRepository errors;
    private final MessageClassificationRepository classifications;
    private final PipelineLatencyRepository pipelineLatency;

    public TelemetryIngestController(HeartbeatRepository heartbeats, TripService tripService,
                                     SosEventRepository sosEvents, ButtonEventRepository buttons,
                                     ErrorEventRepository errors,
                                     MessageClassificationRepository classifications,
                                     PipelineLatencyRepository pipelineLatency) {
        this.heartbeats = heartbeats; this.tripService = tripService; this.sosEvents = sosEvents;
        this.buttons = buttons;
        this.errors = errors;
        this.classifications = classifications;
        this.pipelineLatency = pipelineLatency;
    }

    @PostMapping("/heartbeat")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void heartbeat(@RequestBody List<HeartbeatRequest> batch) {
        heartbeats.saveAll(batch.stream().map(Heartbeat::from).toList());
    }

    @PostMapping("/trips")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void trips(@RequestBody List<TripRequest> batch) {
        batch.forEach(tripService::ingest);
    }

    @PostMapping("/sos")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sos(@RequestBody List<SosRequest> batch) {
        sosEvents.saveAll(batch.stream().map(SosEvent::from).toList());
    }

    @PostMapping("/buttons")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void buttons(@RequestBody List<ButtonEventRequest> batch) {
        buttons.saveAll(batch.stream().map(ButtonEvent::from).toList());
    }

    @PostMapping("/errors")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void errors(@RequestBody List<ErrorEventRequest> batch) {
        errors.saveAll(batch.stream().map(ErrorEvent::from).toList());
    }

    @PostMapping("/classifications")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void classifications(@RequestBody List<ClassificationRequest> batch) {
        classifications.saveAll(batch.stream().map(MessageClassification::from).toList());
    }

    @PostMapping("/pipeline-latency")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void pipelineLatency(@RequestBody List<PipelineLatencyRequest> batch) {
        pipelineLatency.saveAll(batch.stream().map(PipelineLatencyEvent::from).toList());
    }
}