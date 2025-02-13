package com.github.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfiguration {
    public static final String CAPITAL_MAXIMIZATION_QUERY_TOPIC = "capital-maximization-query-events";
    public static final String CAPITAL_MAXIMIZATION_QUERY_DLQ_TOPIC = "capital-maximization-query-events-dlq";

    private final int partitionCount;
    private final int replicaCount;

    public KafkaConfiguration(
            @Value("${spring.cloud.stream.bindings.capital-maximization-query-out-0.producer.partitionCount}")
            int partitionCount,

            @Value("${spring.cloud.stream.kafka.binder.replicationFactor}") int replicaCount) {
        this.partitionCount = partitionCount;
        this.replicaCount = replicaCount;
    }

    @Bean
    public NewTopic capitalMaximizationQueryEventsTopic() {
        return TopicBuilder.name(CAPITAL_MAXIMIZATION_QUERY_TOPIC)
                .partitions(partitionCount)
                .replicas(replicaCount)
                .build();
    }

    @Bean
    public NewTopic capitalMaximizationQueryEventsDLQTopic() {
        return TopicBuilder.name(CAPITAL_MAXIMIZATION_QUERY_DLQ_TOPIC)
                .partitions(1) // Only one partition is needed for the Dead Letter Queue (DLQ)
                .replicas(replicaCount)
                .build();
    }
}