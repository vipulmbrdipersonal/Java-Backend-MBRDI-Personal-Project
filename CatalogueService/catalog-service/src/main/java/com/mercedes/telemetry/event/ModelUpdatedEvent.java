
package com.mercedes.telemetry.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ModelUpdatedEvent(
        String eventId,
        UUID modelId,
        String modelCode,
        String modelName,
        String operation,
        BigDecimal price,
        LocalDateTime occurredAt
) {
}
