
package com.mercedes.telemetry.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedes.telemetry.entity.RevokedToken;
import com.mercedes.telemetry.repository.RevokedTokenRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class AuthTokenRevokedConsumer {

    private final RevokedTokenRepository repository;
    private final ObjectMapper objectMapper;

    public AuthTokenRevokedConsumer(
            RevokedTokenRepository repository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = Topics.AUTH_TOKEN_REVOKED,
            groupId = "catalog-service"
    )
    @Transactional
    public void consume(String message) throws Exception {

        JsonNode event = objectMapper.readTree(message);

        String tokenId = event.path("tokenId").asText();

        if (tokenId == null || tokenId.isBlank()) {
            return;
        }

        if (repository.existsByTokenId(tokenId)) {
            return;
        }

        LocalDateTime expiresAt =
                LocalDateTime.parse(event.path("expiresAt").asText());

        LocalDateTime revokedAt =
                LocalDateTime.parse(event.path("revokedAt").asText());

        RevokedToken token = new RevokedToken();
        token.setTokenId(tokenId);
        token.setExpiresAt(expiresAt);
        token.setRevokedAt(revokedAt);

        repository.save(token);
    }
}
