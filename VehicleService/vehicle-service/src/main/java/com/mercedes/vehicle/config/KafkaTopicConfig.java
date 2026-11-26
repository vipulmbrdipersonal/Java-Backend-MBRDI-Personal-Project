package com.mercedes.vehicle.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {
    @Bean
    KafkaAdmin.NewTopics vehicleTopics(
            @Value("${app.kafka.topic.purchase-completed}") String purchaseCompleted,
            @Value("${app.kafka.topic.vehicle-created}") String vehicleCreated,
            @Value("${app.kafka.topic.vehicle-ownership-assigned}") String ownershipAssigned,
            @Value("${app.kafka.topic.auth-token-revoked}") String tokenRevoked,
            @Value("${app.kafka.partitions:1}") int partitions,
            @Value("${app.kafka.replicas:1}") short replicas) {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(purchaseCompleted).partitions(partitions).replicas(replicas).build(),
                TopicBuilder.name(vehicleCreated).partitions(partitions).replicas(replicas).build(),
                TopicBuilder.name(ownershipAssigned).partitions(partitions).replicas(replicas).build(),
                TopicBuilder.name(tokenRevoked).partitions(partitions).replicas(replicas).build());
    }
}
