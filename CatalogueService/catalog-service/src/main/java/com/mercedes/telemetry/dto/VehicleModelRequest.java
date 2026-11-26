
package com.mercedes.telemetry.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class VehicleModelRequest {

    @NotBlank
    @Size(max = 50)
    private String modelCode;

    @NotBlank
    @Size(max = 150)
    private String modelName;

    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer modelYear;

    @NotBlank
    private String bodyType;

    @NotBlank
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

    @Min(1)
    @Max(20)
    private Integer seatingCapacity;

    private String bootSpace;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    private String currency;
    private String imageUrl;
    private String description;
    private List<String> features;

    public String getModelCode() { return modelCode; }
    public void setModelCode(String modelCode) { this.modelCode = modelCode; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }

    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }

    public String getEngineCapacity() { return engineCapacity; }
    public void setEngineCapacity(String engineCapacity) {
        this.engineCapacity = engineCapacity;
    }

    public String getPower() { return power; }
    public void setPower(String power) { this.power = power; }

    public String getTorque() { return torque; }
    public void setTorque(String torque) { this.torque = torque; }

    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getDrivetrain() { return drivetrain; }
    public void setDrivetrain(String drivetrain) {
        this.drivetrain = drivetrain;
    }

    public String getMileage() { return mileage; }
    public void setMileage(String mileage) { this.mileage = mileage; }

    public String getTopSpeed() { return topSpeed; }
    public void setTopSpeed(String topSpeed) { this.topSpeed = topSpeed; }

    public String getAcceleration() { return acceleration; }
    public void setAcceleration(String acceleration) {
        this.acceleration = acceleration;
    }

    public Integer getSeatingCapacity() { return seatingCapacity; }
    public void setSeatingCapacity(Integer seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }

    public String getBootSpace() { return bootSpace; }
    public void setBootSpace(String bootSpace) {
        this.bootSpace = bootSpace;
    }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) {
        this.features = features;
    }
}
