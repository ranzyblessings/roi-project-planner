package com.github.analytics.event;

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

import static com.github.configuration.KafkaConfiguration.CAPITAL_MAXIMIZATION_QUERY_TOPIC;
import static com.github.projects.model.Validators.requireNonNullOrBlank;

/**
 * Kafka consumer that processes Capital Maximization Query events from the specified Kafka partitions.
 * Currently, logs the events for development purposes.
 */
@Component
public class ProjectCapitalOptimizerEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(ProjectCapitalOptimizerEventConsumer.class);

    private final JsonMapper jsonMapper;
    private final ProjectService projectService;
    private final ProjectCapitalOptimizer projectCapitalOptimizer;

    public ProjectCapitalOptimizerEventConsumer(
            ProjectService projectService,
            ProjectCapitalOptimizer projectCapitalOptimizer) {
        this.jsonMapper = JsonMapper.builder().build();
        this.projectService = projectService;
        this.projectCapitalOptimizer = projectCapitalOptimizer;
    }

    @KafkaListener(
            topicPartitions = @TopicPartition(
                    topic = CAPITAL_MAXIMIZATION_QUERY_TOPIC,
                    partitions = {"0", "1"}
            )
    )
    void handleCapitalMaximizationEvent(final String jsonEvent) {
        logger.info("Starting to process capital maximization event: {}", jsonEvent);

        processCapitalMaximizationEvent(jsonEvent)
                .subscribe(result -> logger.info("Capital maximization completed: {}", result));
    }

    public Mono<ProjectCapitalOptimized> processCapitalMaximizationEvent(final String jsonEvent) {
        return deserializeCapitalMaximizationQueryEvent(jsonEvent)
                .flatMap(event -> projectService.findAll().collectList()
                        .flatMap(projects -> {
                            if (projects.isEmpty()) {
                                throw new IllegalStateException("No projects are available for capital maximization.");
                            }

                            logger.info("Projects count: {}, maxProjects: {}, initialCapital: {}",
                                    projects.size(), event.maxProjects(), event.initialCapital());

                            var query = new CapitalMaximizationQuery(projects, event.maxProjects(), event.initialCapital());
                            return projectCapitalOptimizer.maximizeCapital(query);
                        }))
                .doOnError(error -> logger.error("Error occurred while processing capital maximization event", error));
    }

    private Mono<CapitalMaximizationQueryEvent> deserializeCapitalMaximizationQueryEvent(String jsonEvent) {
        logger.debug("Attempting to convert JSON event to CapitalMaximizationQueryEvent: {}", jsonEvent);

        return Mono.fromCallable(() -> {
                    requireNonNullOrBlank(jsonEvent, () -> "JSON event should not be null or empty");

                    var event = jsonMapper.readValue(jsonEvent, CapitalMaximizationQueryEvent.class);
                    logger.info("Successfully converted JSON event to CapitalMaximizationQueryEvent.");
                    return event;
                }).subscribeOn(Schedulers.boundedElastic()) // Offload to a bounded elastic thread pool for blocking operations;
                .doOnError(error -> logger.error("Failed to convert JSON event to CapitalMaximizationQueryEvent. JSON: {}", jsonEvent, error));
    }
}