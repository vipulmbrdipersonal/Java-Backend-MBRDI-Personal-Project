
package com.mercedes.telemetry.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRegisteredEvent(
        String eventId,
        UUID userId,
        String name,
        String email,
        String role,
        LocalDateTime registeredAt
) {
}
