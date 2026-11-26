
package com.mercedes.telemetry.config;

import com.mercedes.telemetry.event.Topics;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public KafkaAdmin.NewTopics notificationTopics() {

        return new KafkaAdmin.NewTopics(

                // Existing vehicle-health topic
                TopicBuilder.name(Topics.HEALTH_ALERT)
                        .partitions(3)
                        .replicas(1)
                        .build(),

                // Existing notification-created topic
                TopicBuilder.name(Topics.NOTIFICATION_CREATED)
                        .partitions(3)
                        .replicas(1)
                        .build(),

                // Existing token-revocation topic
                TopicBuilder.name(Topics.TOKEN_REVOKED)
                        .partitions(3)
                        .replicas(1)
                        .build(),

                // Password-change OTP email
                TopicBuilder.name(Topics.PASSWORD_CHANGE_OTP)
                        .partitions(3)
                        .replicas(1)
                        .build(),

                // Password-change confirmation email
                TopicBuilder.name(Topics.PASSWORD_CHANGED)
                        .partitions(3)
                        .replicas(1)
                        .build(),

                // Password-reset OTP email
                TopicBuilder.name(Topics.PASSWORD_RESET_OTP)
                        .partitions(3)
                        .replicas(1)
                        .build()
        );
    }
}
