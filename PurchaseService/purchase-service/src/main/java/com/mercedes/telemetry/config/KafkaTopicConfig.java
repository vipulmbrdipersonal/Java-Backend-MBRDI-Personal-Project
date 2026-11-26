
package com.mercedes.telemetry.config;

import com.mercedes.telemetry.event.Topics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public KafkaAdmin.NewTopics purchaseTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(Topics.PURCHASE_COMPLETED)
                        .partitions(3)
                        .replicas(1)
                        .build(),

                TopicBuilder.name(Topics.PURCHASE_FAILED)
                        .partitions(3)
                        .replicas(1)
                        .build(),

                TopicBuilder.name(Topics.AUTH_TOKEN_REVOKED)
                        .partitions(3)
                        .replicas(1)
                        .build()
        );
    }
}
