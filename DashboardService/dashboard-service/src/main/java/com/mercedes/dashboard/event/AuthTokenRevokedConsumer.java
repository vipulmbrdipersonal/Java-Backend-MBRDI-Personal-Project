
package com.mercedes.dashboard.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedes.dashboard.entity.RevokedToken;
import com.mercedes.dashboard.repository.RevokedTokenRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuthTokenRevokedConsumer {

    private final ObjectMapper objectMapper;
    private final RevokedTokenRepository repository;

    public AuthTokenRevokedConsumer(
            ObjectMapper objectMapper,
            RevokedTokenRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @KafkaListener(
            topics = Topics.TOKEN_REVOKED,
            groupId = "dashboard-token-revocation-group"
    )
    public void consume(String message) throws Exception {

        JsonNode node = objectMapper.readTree(message);
        JsonNode jtiNode = node.get("jti");

        if (jtiNode == null || jtiNode.asText().isBlank()) {
            return;
        }

        String jti = jtiNode.asText();

        if (!repository.existsById(jti)) {
            repository.save(new RevokedToken(jti, Instant.now()));
        }
    }
}
