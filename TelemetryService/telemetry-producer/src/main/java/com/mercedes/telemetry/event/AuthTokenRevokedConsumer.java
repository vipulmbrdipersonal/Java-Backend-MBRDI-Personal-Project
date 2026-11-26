
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
    private final RevokedTokenRepository repository;

    public AuthTokenRevokedConsumer(
            ObjectMapper objectMapper,
            RevokedTokenRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @KafkaListener(
            topics = Topics.AUTH_TOKEN_REVOKED,
            groupId = "telemetry-producer-revocation-group")
    public void consume(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);

            JsonNode idNode = json.has("jti")
                    ? json.get("jti") : json.get("tokenId");

            if (idNode == null || !idNode.isTextual()
                    || idNode.asText().isBlank()) {
                log.warn("Revocation event does not contain a token ID");
                return;
            }

            String tokenId = idNode.asText();

            if (!repository.existsById(tokenId)) {
                repository.save(new RevokedToken(tokenId));
            }

            log.info("JWT revocation recorded");
        } catch (Exception ex) {
            log.error("Unable to process JWT revocation event", ex);
        }
    }
}
