
package com.mercedes.telemetry.event;

import java.time.LocalDateTime;

public record TokenRevokedEvent(
        String eventId,
        String tokenId,
        LocalDateTime expiresAt,
        LocalDateTime revokedAt
) {
}
