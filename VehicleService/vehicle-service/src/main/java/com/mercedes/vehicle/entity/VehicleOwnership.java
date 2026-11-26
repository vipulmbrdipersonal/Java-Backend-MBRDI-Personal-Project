package com.mercedes.vehicle.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="vehicle_ownership_history", indexes={@Index(name="idx_ownership_vehicle", columnList="vehicle_id"),@Index(name="idx_ownership_owner",columnList="owner_id")})
public class VehicleOwnership {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="vehicle_id",nullable=false) private Long vehicleId;
    @Column(name="vehicle_code",nullable=false,length=32) private String vehicleCode;
    @Column(name="owner_id",nullable=false,length=100) private String ownerId;
    @Column(name="owner_email",length=180) private String ownerEmail;
    @Column(name="purchase_id",length=100) private String purchaseId;
    @Column(name="owned_from",nullable=false) private Instant ownedFrom;
    @Column(name="owned_until") private Instant ownedUntil;
    @Column(name="current_owner",nullable=false) private boolean currentOwner;
    @PrePersist void onCreate(){if(ownedFrom==null) ownedFrom=Instant.now();}
    public Long getId(){return id;} public Long getVehicleId(){return vehicleId;} public void setVehicleId(Long v){vehicleId=v;}
    public String getVehicleCode(){return vehicleCode;} public void setVehicleCode(String v){vehicleCode=v;} public String getOwnerId(){return ownerId;} public void setOwnerId(String v){ownerId=v;}
    public String getOwnerEmail(){return ownerEmail;} public void setOwnerEmail(String v){ownerEmail=v;} public String getPurchaseId(){return purchaseId;} public void setPurchaseId(String v){purchaseId=v;}
    public Instant getOwnedFrom(){return ownedFrom;} public Instant getOwnedUntil(){return ownedUntil;} public void setOwnedUntil(Instant v){ownedUntil=v;}
    public boolean isCurrentOwner(){return currentOwner;} public void setCurrentOwner(boolean v){currentOwner=v;}
}
