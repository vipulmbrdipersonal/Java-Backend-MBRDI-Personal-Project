package com.mercedes.telemetry.dto;

import java.time.Instant;
import java.util.List;

public class VehicleHealthEvent {

    private String eventId;
    private String telemetryEventId;
    private Long vehicleId;
    private String vin;

    private Integer healthScore;
    private String status;
    private List<String> issues;
    private Instant assessedAt;

    private Double speed;
    private Double engineTemperature;
    private Double fuelLevel;
    private Double batteryVoltage;
    private Double odometer;

    private Integer rpm;

    private Double tyrePressureFrontLeft;
    private Double tyrePressureFrontRight;
    private Double tyrePressureRearLeft;
    private Double tyrePressureRearRight;

    private String ownerEmail;

    public VehicleHealthEvent() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTelemetryEventId() {
        return telemetryEventId;
    }

    public void setTelemetryEventId(String telemetryEventId) {
        this.telemetryEventId = telemetryEventId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Integer getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getIssues() {
        return issues;
    }

    public void setIssues(List<String> issues) {
        this.issues = issues;
    }

    public Instant getAssessedAt() {
        return assessedAt;
    }

    public void setAssessedAt(Instant assessedAt) {
        this.assessedAt = assessedAt;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getEngineTemperature() {
        return engineTemperature;
    }

    public void setEngineTemperature(Double engineTemperature) {
        this.engineTemperature = engineTemperature;
    }

    public Double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(Double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public Double getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(Double batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public Double getOdometer() {
        return odometer;
    }

    public void setOdometer(Double odometer) {
        this.odometer = odometer;
    }

    public Integer getRpm() {
        return rpm;
    }

    public void setRpm(Integer rpm) {
        this.rpm = rpm;
    }

    public Double getTyrePressureFrontLeft() {
        return tyrePressureFrontLeft;
    }

    public void setTyrePressureFrontLeft(Double tyrePressureFrontLeft) {
        this.tyrePressureFrontLeft = tyrePressureFrontLeft;
    }

    public Double getTyrePressureFrontRight() {
        return tyrePressureFrontRight;
    }

    public void setTyrePressureFrontRight(Double tyrePressureFrontRight) {
        this.tyrePressureFrontRight = tyrePressureFrontRight;
    }

    public Double getTyrePressureRearLeft() {
        return tyrePressureRearLeft;
    }

    public void setTyrePressureRearLeft(Double tyrePressureRearLeft) {
        this.tyrePressureRearLeft = tyrePressureRearLeft;
    }

    public Double getTyrePressureRearRight() {
        return tyrePressureRearRight;
    }

    public void setTyrePressureRearRight(Double tyrePressureRearRight) {
        this.tyrePressureRearRight = tyrePressureRearRight;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}