
package com.mercedes.telemetry.controller;

import com.mercedes.telemetry.dto.HealthAlertResponse;
import com.mercedes.telemetry.dto.HealthResponse;
import com.mercedes.telemetry.service.VehicleHealthService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final VehicleHealthService healthService;

    public HealthController(VehicleHealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/vehicle/{vehicleId}/latest")
    public HealthResponse getLatest(@PathVariable Long vehicleId) {
        return healthService.getLatest(vehicleId);
    }

    @GetMapping("/vehicle/{vehicleId}/history")
    public List<HealthResponse> getHistory(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return healthService.getHistory(
                vehicleId,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))
        );
    }

    @GetMapping("/vehicle/{vehicleId}/alerts")
    public List<HealthAlertResponse> getAlerts(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return healthService.getAlerts(
                vehicleId,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))
        );
    }
}
