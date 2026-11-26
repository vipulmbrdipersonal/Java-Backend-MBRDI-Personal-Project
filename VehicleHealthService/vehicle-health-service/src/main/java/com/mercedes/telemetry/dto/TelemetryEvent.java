package com.mercedes.telemetry.dto;

import java.time.LocalDateTime;

public class TelemetryEvent {

    private String eventId;
    private Long telemetryId;
    private Long vehicleId;
    private String vin;

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

    private LocalDateTime recordedAt;

    // Verified vehicle owner's email from Telemetry Service
    private String ownerEmail;

    public TelemetryEvent() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getTelemetryId() {
        return telemetryId;
    }

    public void setTelemetryId(Long telemetryId) {
        this.telemetryId = telemetryId;
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

    public void setTyrePressureFrontLeft(Double value) {
        this.tyrePressureFrontLeft = value;
    }

    public Double getTyrePressureFrontRight() {
        return tyrePressureFrontRight;
    }

    public void setTyrePressureFrontRight(Double value) {
        this.tyrePressureFrontRight = value;
    }

    public Double getTyrePressureRearLeft() {
        return tyrePressureRearLeft;
    }

    public void setTyrePressureRearLeft(Double value) {
        this.tyrePressureRearLeft = value;
    }

    public Double getTyrePressureRearRight() {
        return tyrePressureRearRight;
    }

    public void setTyrePressureRearRight(Double value) {
        this.tyrePressureRearRight = value;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}