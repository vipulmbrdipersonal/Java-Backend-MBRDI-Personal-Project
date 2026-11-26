
package com.mercedes.telemetry.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "purchases",
        indexes = {
                @Index(name = "idx_purchase_customer", columnList = "customerEmail"),
                @Index(name = "idx_purchase_status", columnList = "status")
        }
)
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String purchaseNumber;

    @Column(nullable = false, length = 180)
    private String customerEmail;

    @Column(name = "vehicle_catalog_id", nullable = false, length = 36)
    private String vehicleCatalogId;

    @Column(name = "model_year")
    private Integer modelYear;

    @Column(nullable = false, length = 150)
    private String vehicleName;

    @Column(length = 40)
    private String selectedColor;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getPurchaseNumber() {
        return purchaseNumber;
    }

    public void setPurchaseNumber(String value) {
        this.purchaseNumber = value;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String value) {
        this.customerEmail = value;
    }

    public String getVehicleCatalogId() {
        return vehicleCatalogId;
    }

    public void setVehicleCatalogId(String value) {
        this.vehicleCatalogId = value;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer value) {
        this.modelYear = value;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String value) {
        this.vehicleName = value;
    }

    public String getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(String value) {
        this.selectedColor = value;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal value) {
        this.amount = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String value) {
        this.failureReason = value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
