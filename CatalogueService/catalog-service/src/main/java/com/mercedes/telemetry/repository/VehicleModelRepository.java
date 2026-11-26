package com.mercedes.telemetry.repository;

import com.mercedes.telemetry.entity.VehicleModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleModelRepository
        extends JpaRepository<VehicleModel, UUID> {

    Optional<VehicleModel> findByModelCodeIgnoreCase(String modelCode);

    boolean existsByModelCodeIgnoreCase(String modelCode);

    @Query("""
        SELECT DISTINCT v
        FROM VehicleModel v
        WHERE v.active = true
        AND (
            :keyword IS NULL
            OR LOWER(v.modelName) LIKE CONCAT('%', :keyword, '%')
            OR LOWER(v.modelCode) LIKE CONCAT('%', :keyword, '%')
            OR LOWER(v.description) LIKE CONCAT('%', :keyword, '%')
        )
        AND (
            :bodyType IS NULL
            OR LOWER(v.bodyType) = :bodyType
        )
        AND (
            :fuelType IS NULL
            OR LOWER(v.fuelType) = :fuelType
        )
        AND (
            :transmission IS NULL
            OR LOWER(v.transmission) = :transmission
        )
        AND (
            :year IS NULL
            OR v.modelYear = :year
        )
        AND (
            :minPrice IS NULL
            OR v.price >= :minPrice
        )
        AND (
            :maxPrice IS NULL
            OR v.price <= :maxPrice
        )
        ORDER BY v.modelYear DESC, v.modelName ASC
        """)
    List<VehicleModel> searchModels(
            @Param("keyword") String keyword,
            @Param("bodyType") String bodyType,
            @Param("fuelType") String fuelType,
            @Param("transmission") String transmission,
            @Param("year") Integer year,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    List<VehicleModel> findByIdInAndActiveTrue(
            List<UUID> ids
    );

    List<VehicleModel> findByActiveTrueOrderByModelYearDesc();

    List<VehicleModel> findByActiveTrueAndBodyTypeIgnoreCase(
            String bodyType
    );

    List<VehicleModel> findByActiveTrueAndFuelTypeIgnoreCase(
            String fuelType
    );

    @Query("""
        SELECT DISTINCT v.bodyType
        FROM VehicleModel v
        WHERE v.active = true
        AND v.bodyType IS NOT NULL
        ORDER BY v.bodyType
        """)
    List<String> findAvailableBodyTypes();

    @Query("""
        SELECT DISTINCT v.fuelType
        FROM VehicleModel v
        WHERE v.active = true
        AND v.fuelType IS NOT NULL
        ORDER BY v.fuelType
        """)
    List<String> findAvailableFuelTypes();
}