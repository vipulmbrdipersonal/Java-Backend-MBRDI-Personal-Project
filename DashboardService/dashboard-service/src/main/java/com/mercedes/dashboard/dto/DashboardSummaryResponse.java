
package com.mercedes.dashboard.dto;

import java.util.List;

public class DashboardSummaryResponse {

    private long totalVehicles;
    private long healthyVehicles;
    private long warningVehicles;
    private long criticalVehicles;
    private long unreadNotifications;
    private List<VehicleDashboardResponse> vehicles;

    public DashboardSummaryResponse(
            long totalVehicles,
            long healthyVehicles,
            long warningVehicles,
            long criticalVehicles,
            long unreadNotifications,
            List<VehicleDashboardResponse> vehicles) {
        this.totalVehicles = totalVehicles;
        this.healthyVehicles = healthyVehicles;
        this.warningVehicles = warningVehicles;
        this.criticalVehicles = criticalVehicles;
        this.unreadNotifications = unreadNotifications;
        this.vehicles = vehicles;
    }

    public long getTotalVehicles() { return totalVehicles; }
    public long getHealthyVehicles() { return healthyVehicles; }
    public long getWarningVehicles() { return warningVehicles; }
    public long getCriticalVehicles() { return criticalVehicles; }
    public long getUnreadNotifications() { return unreadNotifications; }
    public List<VehicleDashboardResponse> getVehicles() { return vehicles; }
}
