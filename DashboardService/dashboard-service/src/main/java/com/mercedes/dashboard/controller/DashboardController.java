
package com.mercedes.dashboard.controller;

import com.mercedes.dashboard.dto.*;
import com.mercedes.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/vehicles/{vehicleId}")
    public VehicleDashboardResponse getVehicle(
            @PathVariable Long vehicleId) {
        return dashboardService.getVehicle(vehicleId);
    }

    @GetMapping("/vehicles/{vehicleId}/notifications")
    public List<DashboardNotificationResponse> getNotifications(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return dashboardService.getNotifications(vehicleId, page, size);
    }

    @GetMapping("/vehicles/{vehicleId}/unread-count")
    public Map<String, Object> getUnreadCount(
            @PathVariable Long vehicleId) {

        return Map.of(
                "vehicleId", vehicleId,
                "unreadCount",
                dashboardService.getUnreadCount(vehicleId)
        );
    }
}
