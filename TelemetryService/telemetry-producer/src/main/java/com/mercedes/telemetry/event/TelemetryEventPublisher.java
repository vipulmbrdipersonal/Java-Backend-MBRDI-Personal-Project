
package com.mercedes.telemetry.event;

import com.mercedes.telemetry.dto.TelemetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class TelemetryEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(TelemetryEventPublisher.class);

    private final KafkaTemplate<String, TelemetryEvent> kafkaTemplate;

    public TelemetryEventPublisher(
            KafkaTemplate<String, TelemetryEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(TelemetryEvent event) {

        Runnable action = () -> kafkaTemplate.send(
                Topics.TELEMETRY_RAW,
                String.valueOf(event.vehicleId()),
                event
        ).whenComplete((result, error) -> {
            if (error != null) {
                log.error("Telemetry Kafka publishing failed: {}",
                        event.eventId(), error);
            } else {
                log.info("Telemetry event published: {}, partition: {}",
                        event.eventId(),
                        result.getRecordMetadata().partition());
            }
        });

        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            action.run();
                        }
                    });
        } else {
            action.run();
        }
    }
}
