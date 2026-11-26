
package com.mercedes.telemetry.config;

import com.mercedes.telemetry.event.Topics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public KafkaAdmin.NewTopics telemetryTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(Topics.TELEMETRY_RAW)
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
