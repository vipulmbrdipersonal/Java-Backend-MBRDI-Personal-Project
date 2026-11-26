
package com.mercedes.telemetry.dto;

import com.mercedes.telemetry.entity.VehicleModel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class VehicleModelResponse {

    private UUID id;
    private String modelCode;
    private String brand;
    private String modelName;
    private Integer modelYear;
    private String bodyType;
    private String fuelType;
    private String engine;
    private String engineCapacity;
    private String power;
    private String torque;
    private String transmission;
    private String drivetrain;
    private String mileage;
    private String topSpeed;
    private String acceleration;
    private Integer seatingCapacity;
    private String bootSpace;
    private BigDecimal price;
    private String currency;
    private String imageUrl;
    private String description;
    private boolean active;
    private List<String> features;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VehicleModelResponse from(VehicleModel model) {

        VehicleModelResponse r = new VehicleModelResponse();

        r.id = model.getId();
        r.modelCode = model.getModelCode();
        r.brand = model.getBrand();
        r.modelName = model.getModelName();
        r.modelYear = model.getModelYear();
        r.bodyType = model.getBodyType();
        r.fuelType = model.getFuelType();
        r.engine = model.getEngine();
        r.engineCapacity = model.getEngineCapacity();
        r.power = model.getPower();
        r.torque = model.getTorque();
        r.transmission = model.getTransmission();
        r.drivetrain = model.getDrivetrain();
        r.mileage = model.getMileage();
        r.topSpeed = model.getTopSpeed();
        r.acceleration = model.getAcceleration();
        r.seatingCapacity = model.getSeatingCapacity();
        r.bootSpace = model.getBootSpace();
        r.price = model.getPrice();
        r.currency = model.getCurrency();
        r.imageUrl = model.getImageUrl();
        r.description = model.getDescription();
        r.active = model.isActive();
        r.features = model.getFeatures()
                .stream()
                .map(f -> f.getFeatureName())
                .toList();
        r.createdAt = model.getCreatedAt();
        r.updatedAt = model.getUpdatedAt();

        return r;
    }

    public UUID getId() { return id; }
    public String getModelCode() { return modelCode; }
    public String getBrand() { return brand; }
    public String getModelName() { return modelName; }
    public Integer getModelYear() { return modelYear; }
    public String getBodyType() { return bodyType; }
    public String getFuelType() { return fuelType; }
    public String getEngine() { return engine; }
    public String getEngineCapacity() { return engineCapacity; }
    public String getPower() { return power; }
    public String getTorque() { return torque; }
    public String getTransmission() { return transmission; }
    public String getDrivetrain() { return drivetrain; }
    public String getMileage() { return mileage; }
    public String getTopSpeed() { return topSpeed; }
    public String getAcceleration() { return acceleration; }
    public Integer getSeatingCapacity() { return seatingCapacity; }
    public String getBootSpace() { return bootSpace; }
    public BigDecimal getPrice() { return price; }
    public String getCurrency() { return currency; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public List<String> getFeatures() { return features; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
