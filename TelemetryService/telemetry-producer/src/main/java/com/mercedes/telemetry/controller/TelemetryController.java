package com.mercedes.telemetry.controller;

import com.mercedes.telemetry.dto.TelemetryRequest;
import com.mercedes.telemetry.dto.TelemetryResponse;
import com.mercedes.telemetry.service.TelemetryService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    /*
     * Existing manual ingestion endpoint.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TelemetryResponse ingest(
            @Valid @RequestBody TelemetryRequest request
    ) {
        return telemetryService.ingest(request);
    }

    /*
     * Generate a new random telemetry record for an owned vehicle.
     */
    @PostMapping("/vehicle/{vehicleCode}/simulate")
    @ResponseStatus(HttpStatus.CREATED)
    public TelemetryResponse simulate(
            @PathVariable String vehicleCode,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return telemetryService.simulate(
                vehicleCode,
                authorization
        );
    }

    @GetMapping("/vehicle/{vehicleId}/latest")
    public TelemetryResponse getLatest(
            @PathVariable Long vehicleId
    ) {
        return telemetryService.getLatest(vehicleId);
    }

    @GetMapping("/vehicle/{vehicleId}/history")
    public Page<TelemetryResponse> getHistory(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return telemetryService.getHistory(vehicleId, page, size);
    }
}