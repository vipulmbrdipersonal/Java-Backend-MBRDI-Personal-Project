
package com.mercedes.telemetry.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseEvent(
        String eventId,
        String purchaseNumber,
        String userId,
        String customerEmail,
        String vehicleCatalogId,
        Integer modelYear,
        String vehicleName,
        String selectedColor,
        BigDecimal amount,
        String status,
        String transactionId,
        LocalDateTime occurredAt
) {}
