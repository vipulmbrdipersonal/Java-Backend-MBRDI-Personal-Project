package com.mercedes.telemetry.service;

import com.mercedes.telemetry.dto.*;
import com.mercedes.telemetry.entity.*;
import com.mercedes.telemetry.event.*;
import com.mercedes.telemetry.exception.ApiException;
import com.mercedes.telemetry.repository.VehicleModelRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final VehicleModelRepository repository;
    private final KafkaEventPublisher publisher;

    @Value("${app.catalog.currency:INR}")
    private String defaultCurrency;

    public CatalogService(
            VehicleModelRepository repository,
            KafkaEventPublisher publisher) {

        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional(readOnly = true)
    public List<VehicleModelResponse> getAllModels() {

        return repository.findByActiveTrueOrderByModelYearDesc()
                .stream()
                .map(VehicleModelResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleModelResponse getModel(UUID id) {

        return VehicleModelResponse.from(findActiveModel(id));
    }

    @Transactional(readOnly = true)
    public VehicleModelResponse getByModelCode(String code) {

        VehicleModel model = repository
                .findByModelCodeIgnoreCase(code)
                .filter(VehicleModel::isActive)
                .orElseThrow(() ->
                        new ApiException(
                                "Vehicle model not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        return VehicleModelResponse.from(model);
    }

    @Transactional(readOnly = true)
    public List<VehicleModelResponse> search(
            String keyword,
            String bodyType,
            String fuelType,
            String transmission,
            Integer year,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        if (minPrice != null && minPrice.signum() < 0) {
            throw new ApiException(
                    "Minimum price cannot be negative",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new ApiException(
                    "Maximum price cannot be negative",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new ApiException(
                    "Minimum price cannot exceed maximum price",
                    HttpStatus.BAD_REQUEST
            );
        }

        return repository.searchModels(
                        clean(keyword),
                        clean(bodyType),
                        clean(fuelType),
                        clean(transmission),
                        year,
                        minPrice,
                        maxPrice
                )
                .stream()
                .map(VehicleModelResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompareResponse compare(List<UUID> ids) {

        if (ids == null || ids.size() < 2 || ids.size() > 4) {
            throw new ApiException(
                    "Select between 2 and 4 models to compare",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<UUID> uniqueIds = ids.stream()
                .distinct()
                .toList();

        if (uniqueIds.size() != ids.size()) {
            throw new ApiException(
                    "Duplicate model IDs are not allowed",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<VehicleModel> models =
                repository.findByIdInAndActiveTrue(uniqueIds);

        if (models.size() != uniqueIds.size()) {
            throw new ApiException(
                    "One or more selected models were not found",
                    HttpStatus.NOT_FOUND
            );
        }

        Map<UUID, VehicleModel> modelMap =
                models.stream()
                        .collect(Collectors.toMap(
                                VehicleModel::getId,
                                model -> model
                        ));

        List<VehicleModelResponse> ordered = uniqueIds.stream()
                .map(modelMap::get)
                .map(VehicleModelResponse::from)
                .toList();

        return new CompareResponse(ordered);
    }

    @Transactional(readOnly = true)
    public Map<String, List<String>> getFilters() {

        Map<String, List<String>> filters = new LinkedHashMap<>();

        filters.put(
                "bodyTypes",
                repository.findAvailableBodyTypes()
        );

        filters.put(
                "fuelTypes",
                repository.findAvailableFuelTypes()
        );

        filters.put(
                "transmissions",
                getAllModels().stream()
                        .map(VehicleModelResponse::getTransmission)
                        .filter(Objects::nonNull)
                        .distinct()
                        .sorted()
                        .toList()
        );

        return filters;
    }

    @Transactional
    public VehicleModelResponse create(
            VehicleModelRequest request) {

        String code = request.getModelCode().trim();

        if (repository.existsByModelCodeIgnoreCase(code)) {
            throw new ApiException(
                    "Model code already exists",
                    HttpStatus.CONFLICT
            );
        }

        VehicleModel model = new VehicleModel();

        applyRequest(model, request);

        model.setModelCode(code);
        model.setBrand("Mercedes-Benz");
        model.setActive(true);

        VehicleModel saved = repository.saveAndFlush(model);

        publish(saved, "CREATED");

        return VehicleModelResponse.from(saved);
    }

    @Transactional
    public VehicleModelResponse update(
            UUID id,
            VehicleModelRequest request) {

        VehicleModel model = findActiveModel(id);

        String newCode = request.getModelCode().trim();

        repository.findByModelCodeIgnoreCase(newCode)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ApiException(
                            "Model code already exists",
                            HttpStatus.CONFLICT
                    );
                });

        applyRequest(model, request);

        model.setModelCode(newCode);
        model.setBrand("Mercedes-Benz");

        VehicleModel saved = repository.saveAndFlush(model);

        publish(saved, "UPDATED");

        return VehicleModelResponse.from(saved);
    }

    @Transactional
    public void deactivate(UUID id) {

        VehicleModel model = findActiveModel(id);

        model.setActive(false);

        VehicleModel saved = repository.saveAndFlush(model);

        publish(saved, "DEACTIVATED");
    }

    private VehicleModel findActiveModel(UUID id) {

        return repository.findById(id)
                .filter(VehicleModel::isActive)
                .orElseThrow(() ->
                        new ApiException(
                                "Vehicle model not found",
                                HttpStatus.NOT_FOUND
                        )
                );
    }

    private void applyRequest(
            VehicleModel model,
            VehicleModelRequest request) {

        model.setModelName(request.getModelName().trim());
        model.setModelYear(request.getModelYear());
        model.setBodyType(request.getBodyType().trim());
        model.setFuelType(request.getFuelType().trim());
        model.setEngine(request.getEngine());
        model.setEngineCapacity(request.getEngineCapacity());
        model.setPower(request.getPower());
        model.setTorque(request.getTorque());
        model.setTransmission(request.getTransmission());
        model.setDrivetrain(request.getDrivetrain());
        model.setMileage(request.getMileage());
        model.setTopSpeed(request.getTopSpeed());
        model.setAcceleration(request.getAcceleration());
        model.setSeatingCapacity(request.getSeatingCapacity());
        model.setBootSpace(request.getBootSpace());
        model.setPrice(request.getPrice());

        model.setCurrency(
                request.getCurrency() == null
                        || request.getCurrency().isBlank()
                        ? defaultCurrency
                        : request.getCurrency()
                        .trim()
                        .toUpperCase()
        );

        model.setImageUrl(request.getImageUrl());
        model.setDescription(request.getDescription());

        List<ModelFeature> features =
                request.getFeatures() == null
                        ? Collections.emptyList()
                        : request.getFeatures().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(value -> !value.isEmpty())
                        .distinct()
                        .map(ModelFeature::new)
                        .toList();

        model.setFeatures(features);
    }

    private String clean(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase();
    }

    private void publish(
            VehicleModel model,
            String operation) {

        ModelUpdatedEvent event =
                new ModelUpdatedEvent(
                        UUID.randomUUID().toString(),
                        model.getId(),
                        model.getModelCode(),
                        model.getModelName(),
                        operation,
                        model.getPrice(),
                        LocalDateTime.now()
                );

        publisher.publishModelUpdated(event);
    }
}