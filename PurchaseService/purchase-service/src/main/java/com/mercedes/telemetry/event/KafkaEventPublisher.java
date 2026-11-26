
package com.mercedes.telemetry.event;

import com.mercedes.telemetry.dto.PurchaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class KafkaEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, PurchaseEvent> kafkaTemplate;

    public KafkaEventPublisher(
            KafkaTemplate<String, PurchaseEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCompleted(PurchaseEvent event) {
        publishAfterCommit(Topics.PURCHASE_COMPLETED, event);
    }

    public void publishFailed(PurchaseEvent event) {
        publishAfterCommit(Topics.PURCHASE_FAILED, event);
    }

    private void publishAfterCommit(String topic, PurchaseEvent event) {
        Runnable publish = () -> kafkaTemplate
                .send(topic, event.purchaseNumber(), event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Failed to publish {} for purchase {}",
                                topic, event.purchaseNumber(), error);
                    } else {
                        log.info("Published {} for purchase {}",
                                topic, event.purchaseNumber());
                    }
                });

        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            publish.run();
                        }
                    });
        } else {
            publish.run();
        }
    }
}
