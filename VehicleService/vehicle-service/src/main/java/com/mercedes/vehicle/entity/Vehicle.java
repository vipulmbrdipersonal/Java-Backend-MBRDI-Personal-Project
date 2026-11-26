
package com.mercedes.vehicle.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "vehicles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_vehicle_vin",
                        columnNames = "vin"
                ),
                @UniqueConstraint(
                        name = "uk_vehicle_code",
                        columnNames = "vehicle_code"
                ),
                @UniqueConstraint(
                        name = "uk_vehicle_purchase",
                        columnNames = "purchase_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_vehicle_owner",
                        columnList = "current_owner_id"
                )
        }
)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "vehicle_code",
            nullable = false,
            unique = true,
            length = 32
    )
    private String vehicleCode;

    @Column(nullable = false, unique = true, length = 17)
    private String vin;

    @Column(nullable = false, length = 100)
    private String make = "Mercedes-Benz";

    @Column(nullable = false, length = 120)
    private String model;

    @Column(name = "model_year")
    private Integer modelYear;

    @Column(name = "model_id", length = 80)
    private String modelId;

    @Column(name = "selected_color", length = 60)
    private String selectedColor;

    @Column(
            name = "current_owner_id",
            nullable = false,
            length = 100
    )
    private String currentOwnerId;

    @Column(name = "current_owner_email", length = 180)
    private String currentOwnerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Column(name = "purchase_id", length = 100)
    private String purchaseId;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(String selectedColor) {
        this.selectedColor = selectedColor;
    }

    public String getCurrentOwnerId() {
        return currentOwnerId;
    }

    public void setCurrentOwnerId(String currentOwnerId) {
        this.currentOwnerId = currentOwnerId;
    }

    public String getCurrentOwnerEmail() {
        return currentOwnerEmail;
    }

    public void setCurrentOwnerEmail(String currentOwnerEmail) {
        this.currentOwnerEmail = currentOwnerEmail;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
