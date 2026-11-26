
package com.mercedes.telemetry.config;

import com.mercedes.telemetry.event.Topics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public KafkaAdmin.NewTopics healthTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(Topics.TELEMETRY_RAW)
                        .partitions(3).replicas(1).build(),

                TopicBuilder.name(Topics.HEALTH_UPDATED)
                        .partitions(3).replicas(1).build(),

                TopicBuilder.name(Topics.HEALTH_ALERT)
                        .partitions(3).replicas(1).build(),

                TopicBuilder.name(Topics.TOKEN_REVOKED)
                        .partitions(3).replicas(1).build()
        );
    }
}
