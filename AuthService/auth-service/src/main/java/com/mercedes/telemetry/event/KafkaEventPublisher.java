
package com.mercedes.telemetry.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserRegistered(
            UserRegisteredEvent event) {

        kafkaTemplate.send(
                Topics.USER_REGISTERED,
                event.userId().toString(),
                event
        );
    }

    public void publishTokenRevoked(
            TokenRevokedEvent event) {

        kafkaTemplate.send(
                Topics.AUTH_TOKEN_REVOKED,
                event.tokenId(),
                event
        );
    }
}
