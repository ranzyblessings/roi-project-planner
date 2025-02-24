package com.github.analytics.event;

import com.github.analytics.api.CapitalMaximizationQuery;
import com.github.analytics.api.ProjectCapitalOptimized;
import com.github.analytics.api.ProjectCapitalOptimizer;
import com.github.projects.api.ProjectService;
import com.github.projects.exception.ProjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
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

    private final ProjectService projectService;
    private final ProjectCapitalOptimizer projectCapitalOptimizer;

    public ProjectCapitalOptimizerEventConsumer(ProjectService projectService, ProjectCapitalOptimizer projectCapitalOptimizer) {
        this.projectService = projectService;
        this.projectCapitalOptimizer = projectCapitalOptimizer;
    }

    /**
     * Kafka listener that consumes capital maximization query events from Kafka topic partitions.
     */
    @KafkaListener(
            topicPartitions = @TopicPartition(
                    topic = CAPITAL_MAXIMIZATION_QUERY_TOPIC,
                    partitions = {"0", "1"}
            )
    )
    public void handleCapitalMaximizationEvent(
            @Payload CapitalMaximizationQueryEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        logger.info("Received capital maximization event: {} from partition: {}", event, partition);

        processCapitalMaximizationEvent(event)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))) // Retry transient failures
                .doOnError(error -> logger.error("Final failure processing event", error))
                .onErrorResume(error -> Mono.empty()) // Avoid infinite Kafka retries
                .subscribe(result -> logger.info("Processing completed. Final capital: {}, Selected projects: {}",
                        result.finalCapital(), result.selectedProjects().size())
                );
    }

    public Mono<ProjectCapitalOptimized> processCapitalMaximizationEvent(CapitalMaximizationQueryEvent event) {
        return projectService.findAll().collectList()
                .flatMap(projects -> {
                    if (projects.isEmpty()) {
                        logger.warn("No projects available for capital maximization.");
                        return Mono.error(new ProjectNotFoundException("No projects available for capital maximization."));
                    }

                    logger.info("Processing event: maxProjects={}, initialCapital={}, availableProjects={}",
                            event.maxProjects(), event.initialCapital(), projects.size());

                    var query = new CapitalMaximizationQuery(projects, event.maxProjects(), event.initialCapital());
                    return projectCapitalOptimizer.maximizeCapital(query);
                })
                .doOnError(error -> logger.error("Error during capital maximization process", error));
    }
}