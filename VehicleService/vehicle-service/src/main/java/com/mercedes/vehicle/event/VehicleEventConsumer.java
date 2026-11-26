
package com.mercedes.vehicle.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.mercedes.vehicle.dto.VehicleResponse;
import com.mercedes.vehicle.entity.RevokedToken;
import com.mercedes.vehicle.entity.Vehicle;
import com.mercedes.vehicle.entity.VehicleOwnership;
import com.mercedes.vehicle.entity.VehicleStatus;
import com.mercedes.vehicle.repository.RevokedTokenRepository;
import com.mercedes.vehicle.repository.VehicleOwnershipRepository;
import com.mercedes.vehicle.repository.VehicleRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Component
public class VehicleEventConsumer {

    private final VehicleRepository vehicles;
    private final VehicleOwnershipRepository ownerships;
    private final RevokedTokenRepository revoked;
    private final KafkaEventPublisher events;

    public VehicleEventConsumer(
            VehicleRepository vehicles,
            VehicleOwnershipRepository ownerships,
            RevokedTokenRepository revoked,
            KafkaEventPublisher events) {

        this.vehicles = vehicles;
        this.ownerships = ownerships;
        this.revoked = revoked;
        this.events = events;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.purchase-completed}",
            groupId = "vehicle-service-purchase-group"
    )
    @Transactional
    public void onPurchaseCompleted(JsonNode event) {

        String purchaseId = text(
                event,
                "purchaseNumber",
                "purchaseId",
                "id"
        );

        String ownerId = text(
                event,
                "userId",
                "ownerId",
                "customerId"
        );

        String purchaseStatus = text(event, "status");

        // Never create a vehicle without a valid purchase and owner.
        if (isBlank(purchaseId) || isBlank(ownerId)) {
            return;
        }

        // Only explicitly completed purchases may create vehicles.
        if (!"COMPLETED".equalsIgnoreCase(purchaseStatus)) {
            return;
        }

        // Idempotency: one vehicle per successful purchase.
        if (vehicles.existsByPurchaseId(purchaseId)) {
            return;
        }

        String model = text(
                event,
                "vehicleName",
                "model",
                "modelName",
                "vehicleModel"
        );

        String modelId = text(
                event,
                "vehicleCatalogId",
                "modelId",
                "catalogModelId"
        );

        String selectedColor = text(
                event,
                "selectedColor",
                "color"
        );

        String ownerEmail = text(
                event,
                "customerEmail",
                "email",
                "ownerEmail"
        );

        Integer modelYear = integer(
                event,
                "modelYear",
                "year"
        );

        if (isBlank(model)) {
            model = "Mercedes-Benz Vehicle";
        }

        Vehicle vehicle = new Vehicle();

        vehicle.setVehicleCode(generateVehicleCode());
        vehicle.setVin(generateVin());
        vehicle.setMake("Mercedes-Benz");
        vehicle.setModel(model);
        vehicle.setModelId(modelId);
        vehicle.setModelYear(modelYear);
        vehicle.setSelectedColor(selectedColor);
        vehicle.setCurrentOwnerId(ownerId);
        vehicle.setCurrentOwnerEmail(ownerEmail);
        vehicle.setPurchaseId(purchaseId);
        vehicle.setStatus(VehicleStatus.ACTIVE);

        Vehicle saved = vehicles.saveAndFlush(vehicle);

        recordOwnership(
                saved,
                ownerId,
                ownerEmail,
                purchaseId
        );

        VehicleResponse response = VehicleResponse.from(saved);

        events.publishVehicleCreated(response);
        events.publishOwnershipAssigned(response);
    }

    @KafkaListener(
            topics = "${app.kafka.topic.auth-token-revoked}",
            groupId = "vehicle-service-revocation-group"
    )
    @Transactional
    public void onTokenRevoked(JsonNode event) {

        String tokenId = text(event, "tokenId");

        if (isBlank(tokenId) || revoked.existsById(tokenId)) {
            return;
        }

        Instant expiresAt = instant(event, "expiresAt");
        Instant revokedAt = instant(event, "revokedAt");

        revoked.save(
                new RevokedToken(
                        tokenId,
                        expiresAt,
                        revokedAt == null ? Instant.now() : revokedAt
                )
        );
    }

    private void recordOwnership(
            Vehicle vehicle,
            String ownerId,
            String email,
            String purchaseId) {

        VehicleOwnership ownership = new VehicleOwnership();

        ownership.setVehicleId(vehicle.getId());
        ownership.setVehicleCode(vehicle.getVehicleCode());
        ownership.setOwnerId(ownerId);
        ownership.setOwnerEmail(email);
        ownership.setPurchaseId(purchaseId);
        ownership.setCurrentOwner(true);

        ownerships.save(ownership);
    }

    private String generateVehicleCode() {

        return "MB-VEH-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase(Locale.ROOT);
    }

    private String generateVin() {

        return "W1K" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 14)
                        .toUpperCase(Locale.ROOT);
    }

    private String text(JsonNode node, String... keys) {

        for (String key : keys) {

            JsonNode value = node.get(key);

            if (value != null
                    && !value.isNull()
                    && !value.asText().isBlank()) {

                return value.asText();
            }
        }

        return null;
    }

    private Integer integer(JsonNode node, String... keys) {

        for (String key : keys) {

            JsonNode value = node.get(key);

            if (value != null && value.canConvertToInt()) {
                return value.asInt();
            }
        }

        return null;
    }

    private Instant instant(JsonNode node, String key) {

        try {
            String value = text(node, key);

            return value == null ? null : Instant.parse(value);

        } catch (Exception ex) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
