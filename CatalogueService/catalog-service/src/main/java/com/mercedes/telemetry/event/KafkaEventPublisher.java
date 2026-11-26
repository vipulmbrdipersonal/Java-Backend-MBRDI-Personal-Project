
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

    public void publishModelUpdated(ModelUpdatedEvent event) {

        kafkaTemplate.send(
                Topics.MODEL_UPDATED,
                event.modelId().toString(),
                event
        );
    }
}
