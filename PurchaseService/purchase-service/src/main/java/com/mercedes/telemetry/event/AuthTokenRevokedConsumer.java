
package com.mercedes.telemetry.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedes.telemetry.entity.RevokedToken;
import com.mercedes.telemetry.repository.RevokedTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenRevokedConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(AuthTokenRevokedConsumer.class);

    private final ObjectMapper objectMapper;
    private final RevokedTokenRepository revokedTokenRepository;

    public AuthTokenRevokedConsumer(
            ObjectMapper objectMapper,
            RevokedTokenRepository revokedTokenRepository) {
        this.objectMapper = objectMapper;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @KafkaListener(
            topics = Topics.AUTH_TOKEN_REVOKED,
            groupId = "purchase-service-revocation-group")
    public void consume(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);

            String tokenId = text(json, "jti");
            if (tokenId == null) tokenId = text(json, "tokenId");

            if (tokenId == null || tokenId.isBlank()) {
                log.warn("Ignoring revocation event without jti/tokenId");
                return;
            }

            if (!revokedTokenRepository.existsById(tokenId)) {
                revokedTokenRepository.save(new RevokedToken(tokenId));
            }

            log.info("Recorded revoked JWT: {}", tokenId);
        } catch (Exception ex) {
            log.error("Could not process token revocation event", ex);
        }
    }

    private String text(JsonNode json, String field) {
        JsonNode value = json.get(field);
        return value != null && value.isTextual() ? value.asText() : null;
    }
}
