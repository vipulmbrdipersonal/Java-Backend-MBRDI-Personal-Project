
package com.mercedes.telemetry.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseResponse(
        String purchaseNumber,
        String customerEmail,
        String vehicleCatalogId,
        Integer modelYear,
        String vehicleName,
        String selectedColor,
        BigDecimal amount,
        String status,
        String transactionId,
        String paymentStatus,
        String message,
        LocalDateTime createdAt
) {}
