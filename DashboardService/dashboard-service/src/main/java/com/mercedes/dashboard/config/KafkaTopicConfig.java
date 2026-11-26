
package com.mercedes.dashboard.config;

import com.mercedes.dashboard.event.Topics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public KafkaAdmin.NewTopics dashboardTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(Topics.HEALTH_UPDATED)
                        .partitions(3).replicas(1).build(),

                TopicBuilder.name(Topics.NOTIFICATION_CREATED)
                        .partitions(3).replicas(1).build(),

                TopicBuilder.name(Topics.TOKEN_REVOKED)
                        .partitions(3).replicas(1).build()
        );
    }
}
