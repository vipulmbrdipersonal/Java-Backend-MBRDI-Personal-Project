
package com.mercedes.telemetry.service;

import com.mercedes.telemetry.dto.CatalogVehicleResponse;
import com.mercedes.telemetry.dto.PurchaseEvent;
import com.mercedes.telemetry.dto.PurchaseRequest;
import com.mercedes.telemetry.dto.PurchaseResponse;
import com.mercedes.telemetry.entity.Payment;
import com.mercedes.telemetry.entity.Purchase;
import com.mercedes.telemetry.event.KafkaEventPublisher;
import com.mercedes.telemetry.repository.PaymentRepository;
import com.mercedes.telemetry.repository.PurchaseRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PaymentRepository paymentRepository;
    private final CatalogClient catalogClient;
    private final DummyPaymentGateway paymentGateway;
    private final KafkaEventPublisher eventPublisher;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            PaymentRepository paymentRepository,
            CatalogClient catalogClient,
            DummyPaymentGateway paymentGateway,
            KafkaEventPublisher eventPublisher) {

        this.purchaseRepository = purchaseRepository;
        this.paymentRepository = paymentRepository;
        this.catalogClient = catalogClient;
        this.paymentGateway = paymentGateway;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PurchaseResponse createPurchase(
            String customerEmail,
            String userId,
            PurchaseRequest request) {

        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user ID is required"
            );
        }

        CatalogVehicleResponse vehicle =
                catalogClient.getVehicle(request.vehicleCatalogId());

        if (request.selectedColor() == null
                || request.selectedColor().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a vehicle colour"
            );
        }

        String selectedColor = request.selectedColor().trim();

        String purchaseNumber = "MB-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase();

        Purchase purchase = new Purchase();

        purchase.setPurchaseNumber(purchaseNumber);
        purchase.setCustomerEmail(customerEmail);
        purchase.setVehicleCatalogId(vehicle.id().toString());
        purchase.setModelYear(vehicle.modelYear());
        purchase.setVehicleName(vehicle.modelName());
        purchase.setSelectedColor(selectedColor);
        purchase.setAmount(vehicle.price());
        purchase.setStatus("PROCESSING");

        purchase = purchaseRepository.saveAndFlush(purchase);

        DummyPaymentGateway.PaymentResult result =
                paymentGateway.process(
                        vehicle.price(),
                        request.paymentMethod()
                );

        Payment payment = new Payment();

        payment.setPurchase(purchase);
        payment.setTransactionId(result.transactionId());
        payment.setAmount(vehicle.price());
        payment.setStatus(
                result.successful() ? "SUCCESS" : "FAILED"
        );
        payment.setGatewayMessage(result.message());

        paymentRepository.save(payment);

        if (result.successful()) {
            purchase.setStatus("COMPLETED");
            purchase.setFailureReason(null);
        } else {
            purchase.setStatus("FAILED");
            purchase.setFailureReason(result.message());
        }

        purchaseRepository.save(purchase);

        PurchaseEvent event =
                toEvent(purchase, result.transactionId(), userId);

        if (result.successful()) {
            eventPublisher.publishCompleted(event);
        } else {
            eventPublisher.publishFailed(event);
        }

        return toResponse(purchase, payment, result.message());
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getMyPurchases(
            String customerEmail) {

        return purchaseRepository
                .findByCustomerEmailOrderByCreatedAtDesc(customerEmail)
                .stream()
                .map(purchase -> {
                    Payment payment = paymentRepository
                            .findByPurchaseId(purchase.getId())
                            .orElse(null);

                    return toResponse(purchase, payment, null);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseResponse getPurchase(
            String purchaseNumber,
            String requesterEmail,
            boolean admin) {

        Purchase purchase = purchaseRepository
                .findByPurchaseNumber(purchaseNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Purchase not found"
                ));

        if (!admin &&
                !purchase.getCustomerEmail()
                        .equalsIgnoreCase(requesterEmail)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this purchase"
            );
        }

        Payment payment = paymentRepository
                .findByPurchaseId(purchase.getId())
                .orElse(null);

        return toResponse(purchase, payment, null);
    }

    private PurchaseEvent toEvent(
            Purchase purchase,
            String transactionId,
            String userId) {

        return new PurchaseEvent(
                UUID.randomUUID().toString(),
                purchase.getPurchaseNumber(),
                userId,
                purchase.getCustomerEmail(),
                purchase.getVehicleCatalogId(),
                purchase.getModelYear(),
                purchase.getVehicleName(),
                purchase.getSelectedColor(),
                purchase.getAmount(),
                purchase.getStatus(),
                transactionId,
                LocalDateTime.now()
        );
    }

    private PurchaseResponse toResponse(
            Purchase purchase,
            Payment payment,
            String message) {

        return new PurchaseResponse(
                purchase.getPurchaseNumber(),
                purchase.getCustomerEmail(),
                purchase.getVehicleCatalogId(),
                purchase.getModelYear(),
                purchase.getVehicleName(),
                purchase.getSelectedColor(),
                purchase.getAmount(),
                purchase.getStatus(),
                payment == null ? null : payment.getTransactionId(),
                payment == null ? null : payment.getStatus(),
                message == null && payment != null
                        ? payment.getGatewayMessage()
                        : message,
                purchase.getCreatedAt()
        );
    }
}
