
package com.mercedes.telemetry.config;

import com.mercedes.telemetry.event.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic userRegisteredTopic() {
        return TopicBuilder.name(Topics.USER_REGISTERED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic authTokenRevokedTopic() {
        return TopicBuilder.name(Topics.AUTH_TOKEN_REVOKED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
