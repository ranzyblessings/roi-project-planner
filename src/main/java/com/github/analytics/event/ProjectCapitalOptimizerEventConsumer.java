package com.github.analytics.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.analytics.api.CapitalMaximizationQuery;
import com.github.analytics.api.ProjectCapitalOptimized;
import com.github.analytics.api.ProjectCapitalOptimizer;
import com.github.projects.api.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

import static com.github.configuration.KafkaConfiguration.CAPITAL_MAXIMIZATION_QUERY_TOPIC;

/**
 * Kafka consumer that processes Capital Maximization Query events from the specified Kafka partitions.
 * Processes capital maximization events and optimizes project selection. Currently, logs events for debugging.
 */
@Component
public class ProjectCapitalOptimizerEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(ProjectCapitalOptimizerEventConsumer.class);
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private final ProjectService projectService;
    private final ProjectCapitalOptimizer projectCapitalOptimizer;

    public ProjectCapitalOptimizerEventConsumer(
            ProjectService projectService,
            ProjectCapitalOptimizer projectCapitalOptimizer) {
        this.projectService = projectService;
        this.projectCapitalOptimizer = projectCapitalOptimizer;
    }

    /**
     * Kafka listener that consumes capital maximization query events from Kafka topic partitions.
     *
     * @param jsonEvent The JSON event as a String.
     */
    @KafkaListener(
            topicPartitions = @TopicPartition(
                    topic = CAPITAL_MAXIMIZATION_QUERY_TOPIC,
                    partitions = {"0", "1"}
            )
    )
    public void handleCapitalMaximizationEvent(final String jsonEvent) {
        logger.info("Received capital maximization event: {}", jsonEvent);

        processCapitalMaximizationEvent(jsonEvent)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))) // Retry transient failures
                .doOnError(error -> logger.error("Final failure processing event: {}", jsonEvent, error))
                .onErrorResume(error -> Mono.empty()) // Avoid infinite Kafka retries
                .subscribe(
                        result -> logger.info("Processing completed. Final capital: {}, Selected projects: {}",
                                result.finalCapital(), result.selectedProjects().size())
                );
    }

    /**
     * Processes a capital maximization event by deserializing the JSON event, fetching available projects,
     * and optimizing capital allocation.
     *
     * @param jsonEvent The JSON event payload.
     * @return A Mono of {@link ProjectCapitalOptimized} containing the optimization result.
     */
    public Mono<ProjectCapitalOptimized> processCapitalMaximizationEvent(final String jsonEvent) {
        return parseCapitalMaximizationJsonEvent(jsonEvent)
                .flatMap(event -> projectService.findAll().collectList()
                        .flatMap(projects -> {
                            if (projects.isEmpty()) {
                                logger.warn("No projects available for capital maximization.");
                                return Mono.error(new IllegalStateException("No projects available for capital maximization."));
                            }

                            logger.info("Processing event: maxProjects={}, initialCapital={}, availableProjects={}",
                                    event.maxProjects(), event.initialCapital(), projects.size());

                            var query = new CapitalMaximizationQuery(projects, event.maxProjects(), event.initialCapital());
                            return projectCapitalOptimizer.maximizeCapital(query);
                        }))
                .doOnError(error -> logger.error("Error during capital maximization process", error));
    }

    /**
     * Parses and deserializes the JSON event into a {@link CapitalMaximizationQueryEvent} object.
     *
     * @param jsonEvent The JSON event payload.
     * @return A Mono of the deserialized {@link CapitalMaximizationQueryEvent}.
     */
    private Mono<CapitalMaximizationQueryEvent> parseCapitalMaximizationJsonEvent(String jsonEvent) {
        logger.info("Attempting to deserialize JSON event: {}", jsonEvent);

        return Mono.fromCallable(() -> {
                    if (jsonEvent == null || jsonEvent.trim().isEmpty()) {
                        throw new IllegalArgumentException("JSON event should not be null or empty");
                    }

                    try {
                        var event = JSON_MAPPER.readValue(jsonEvent, CapitalMaximizationQueryEvent.class);
                        logger.info("Successfully deserialized event: {}", event);
                        return event;
                    } catch (JsonProcessingException e) {
                        logger.error("Invalid JSON format: {} | Error: {}", jsonEvent, e.getMessage(), e);
                        throw new IllegalArgumentException("Invalid JSON format", e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic()) // Offload JSON parsing to a separate thread
                .doOnError(error -> logger.error("Deserialization failed for JSON: {}", jsonEvent, error));
    }
}