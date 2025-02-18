package com.github.analytics.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

import static com.github.projects.model.Validators.requireNonNull;

/**
 * Kafka publisher component responsible for publishing capital maximization query events
 * to a designated Kafka topic.
 */
@Component
public class ProjectCapitalOptimizerEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(ProjectCapitalOptimizerEventPublisher.class);
    private static final String PARTITION_KEY_HEADER = "PARTITION_KEY";
    private static final String CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING = "capital-maximization-query-out-0";

    private final StreamBridge streamBridge;

    public ProjectCapitalOptimizerEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public Mono<Boolean> publishEvent(CapitalMaximizationQueryEvent event) {
        requireNonNull(event, () -> "Capital maximization query event cannot be null");

        logger.info("Publishing event with maxProjects: {}, initialCapital: {}",
                event.maxProjects(), event.initialCapital());

        return Mono.fromCallable(() -> {
                    // Generate a partition key based on event data for better load balancing (round-robin effect)
                    String partitionKey = generatePartitionKey(event);

                    // Build the message with event payload and computed partition key
                    Message<CapitalMaximizationQueryEvent> message = MessageBuilder.withPayload(event)
                            .setHeader(PARTITION_KEY_HEADER, partitionKey)
                            .build();

                    // Send the message and check if it was successful
                    boolean sent = streamBridge.send(CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING, message);

                    if (sent) {
                        logger.info("Capital maximization query event successfully sent with partition key: {}", partitionKey);
                    } else {
                        logger.warn("Kafka event publishing failed for event: {}", event);
                    }

                    return sent;
                })
                .subscribeOn(Schedulers.boundedElastic()) // Offload to an elastic thread pool for blocking operations
                .doOnError(error -> logger.error("Failed to publish capital maximization query event", error));
    }

    /**
     * Computes a deterministic partition key based on event attributes to ensure consistent
     * message routing. The partition key is mapped to a range (0-9), promoting even distribution
     * of messages across available partitions.
     */
    private String generatePartitionKey(CapitalMaximizationQueryEvent event) {
        final int hash = Objects.hash(event.maxProjects(), event.initialCapital());
        return Integer.toString(Math.abs(hash) % 10);
    }
}