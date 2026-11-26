
package com.mercedes.telemetry.event;

import com.mercedes.telemetry.dto.PasswordChangeOtpEmailEvent;
import com.mercedes.telemetry.dto.PasswordChangedEmailEvent;
import com.mercedes.telemetry.dto.PasswordResetOtpEmailEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class PasswordEmailEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(PasswordEmailEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PasswordEmailEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOtpAfterCommit(
            PasswordChangeOtpEmailEvent event
    ) {
        publishAfterCommit(() ->
                sendEvent(
                        PasswordEmailTopics.PASSWORD_CHANGE_OTP,
                        event.getEmail(),
                        event
                )
        );
    }

    public void publishPasswordChangedAfterCommit(
            PasswordChangedEmailEvent event
    ) {
        publishAfterCommit(() ->
                sendEvent(
                        PasswordEmailTopics.PASSWORD_CHANGED,
                        event.getEmail(),
                        event
                )
        );
    }

    public void publishPasswordResetOtpAfterCommit(
            PasswordResetOtpEmailEvent event
    ) {
        publishAfterCommit(() ->
                sendEvent(
                        PasswordEmailTopics.PASSWORD_RESET_OTP,
                        event.getEmail(),
                        event
                )
        );
    }

    private void publishAfterCommit(Runnable action) {

        if (
                TransactionSynchronizationManager
                        .isSynchronizationActive()
                        &&
                        TransactionSynchronizationManager
                                .isActualTransactionActive()
        ) {
            TransactionSynchronizationManager
                    .registerSynchronization(
                            new TransactionSynchronization() {
                                @Override
                                public void afterCommit() {
                                    action.run();
                                }
                            }
                    );
        } else {
            action.run();
        }
    }

    private void sendEvent(
            String topic,
            String key,
            Object event
    ) {
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, exception) -> {

                    if (exception != null) {
                        log.error(
                                "Unable to publish password email event. Topic: {}",
                                topic,
                                exception
                        );
                    } else {
                        log.info(
                                "Password email event published. Topic: {}",
                                topic
                        );
                    }
                });
    }
}
