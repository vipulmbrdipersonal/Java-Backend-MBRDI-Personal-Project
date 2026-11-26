
package com.mercedes.telemetry.controller;

import com.mercedes.telemetry.dto.*;
import com.mercedes.telemetry.service.CatalogService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalog")
public class VehicleModelController {

    private final CatalogService catalogService;

    public VehicleModelController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // ==========================================
    // GET ALL ACTIVE VEHICLES
    // ==========================================

    @GetMapping("/models")
    public ResponseEntity<ApiResponse<List<VehicleModelResponse>>>
    getAllModels() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Vehicle models retrieved successfully",
                        catalogService.getAllModels()
                )
        );
    }

    // ==========================================
    // GET VEHICLE BY ID
    // ==========================================

    @GetMapping("/models/{id}")
    public ResponseEntity<ApiResponse<VehicleModelResponse>>
    getModel(@PathVariable UUID id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Vehicle details retrieved successfully",
                        catalogService.getModel(id)
                )
        );
    }

    // ==========================================
    // GET VEHICLE BY MODEL CODE
    // ==========================================

    @GetMapping("/models/code/{code}")
    public ResponseEntity<ApiResponse<VehicleModelResponse>>
    getByCode(@PathVariable String code) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Vehicle retrieved successfully",
                        catalogService.getByModelCode(code)
                )
        );
    }

    // ==========================================
    // SEARCH AND FILTER
    // ==========================================

    @GetMapping("/models/search")
    public ResponseEntity<ApiResponse<List<VehicleModelResponse>>>
    search(

            @RequestParam(required = false) String keyword,

            @RequestParam(required = false) String bodyType,

            @RequestParam(required = false) String fuelType,

            @RequestParam(required = false) String transmission,

            @RequestParam(required = false) Integer year,

            @RequestParam(required = false) BigDecimal minPrice,

            @RequestParam(required = false) BigDecimal maxPrice

    ) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Search completed successfully",
                        catalogService.search(
                                keyword,
                                bodyType,
                                fuelType,
                                transmission,
                                year,
                                minPrice,
                                maxPrice
                        )
                )
        );
    }

    // ==========================================
    // COMPARE 2 TO 4 VEHICLES
    // ==========================================

    @GetMapping("/models/compare")
    public ResponseEntity<ApiResponse<CompareResponse>>
    compare(@RequestParam String ids) {

        List<UUID> modelIds;

        try {
            modelIds = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .map(UUID::fromString)
                    .toList();

        } catch (IllegalArgumentException exception) {

            throw new com.mercedes.telemetry.exception.ApiException(
                    "Invalid vehicle model ID",
                    HttpStatus.BAD_REQUEST
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Vehicle comparison completed",
                        catalogService.compare(modelIds)
                )
        );
    }

    // ==========================================
    // AVAILABLE FILTER OPTIONS
    // ==========================================

    @GetMapping("/filters")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>>
    getFilters() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Filters retrieved successfully",
                        catalogService.getFilters()
                )
        );
    }

    // ==========================================
    // ADD VEHICLE MODEL (ADMIN)
    // ==========================================

    @PostMapping("/models")
    public ResponseEntity<ApiResponse<VehicleModelResponse>>
    create(@Valid @RequestBody VehicleModelRequest request) {

        VehicleModelResponse response =
                catalogService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                true,
                                "Vehicle model created successfully",
                                response
                        )
                );
    }

    // ==========================================
    // UPDATE VEHICLE MODEL (ADMIN)
    // ==========================================

    @PutMapping("/models/{id}")
    public ResponseEntity<ApiResponse<VehicleModelResponse>>
    update(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleModelRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Vehicle model updated successfully",
                        catalogService.update(id, request)
                )
        );
    }

    // ==========================================
    // DEACTIVATE VEHICLE MODEL (ADMIN)
    // ==========================================

    @DeleteMapping("/models/{id}")
    public ResponseEntity<ApiResponse<Void>>
    deactivate(@PathVariable UUID id) {

        catalogService.deactivate(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Vehicle model deactivated successfully",
                        null
                )
        );
    }
}
