package com.github.analytics.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

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
        logger.info("Preparing to publish capital maximization query event to topic: {}", event);

        return Mono.fromCallable(() -> {
                    requireNonNull(event, () -> "Capital maximization query event cannot be null");

                    // Build the message with the event payload and set the partition key
                    Message<CapitalMaximizationQueryEvent> message = MessageBuilder.withPayload(event)
                            .setHeader(PARTITION_KEY_HEADER, UUID.randomUUID().toString()) // Set partition key for round-robin or even distribution
                            .build();

                    // Send the message and log headers
                    boolean sent = streamBridge.send(CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING, message);
                    message.getHeaders().forEach((key, value) -> logger.info("Message header: {} = {}", key, value));
                    logger.info("Capital maximization query event sent to topic: {}", sent);
                    return sent;
                })
                .subscribeOn(Schedulers.boundedElastic()) // Offload to a bounded elastic thread pool for blocking operations
                .doOnError(error -> logger.error("Failed to publish capital maximization query event to topic", error));
    }
}