package com.github.analytics.event;

import com.github.analytics.api.CapitalMaximizationQuery;
import com.github.analytics.api.ProjectCapitalOptimized;
import com.github.analytics.api.ProjectCapitalOptimizer;
import com.github.configuration.TestcontainersConfiguration;
import com.github.projects.api.ProjectService;
import com.github.projects.model.AuditMetadata;
import com.github.projects.model.ProjectDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.UUID.randomUUID;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProjectCapitalOptimizerIntegrationTest extends TestcontainersConfiguration {

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private ProjectCapitalOptimizer projectCapitalOptimizer;

    private final ProjectCapitalOptimizerEventPublisher publisher;

    @Autowired
    ProjectCapitalOptimizerIntegrationTest(ProjectCapitalOptimizerEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Test
    void givenValidEvent_whenPublished_thenConsumerProcessesEventSuccessfully() {

        // Given: A valid event and mocked dependencies
        var event = new CapitalMaximizationQueryEvent(2, new BigDecimal("100"));

        var project = new ProjectDTO(randomUUID(), "Project 1", new BigDecimal("100.00"),
                new BigDecimal("500.00"), AuditMetadata.empty(), 0L);
        var optimized = new ProjectCapitalOptimized(List.of(project), new BigDecimal("500.00"));

        when(projectService.findAll()).thenReturn(Flux.just(project));
        when(projectCapitalOptimizer.maximizeCapital(any(CapitalMaximizationQuery.class))).thenReturn(Mono.just(optimized));

        // When: Event is published to Kafka topic
        var publishEventMono = publisher.publishEvent(event);

        // Then: Verify event is published successfully
        StepVerifier.create(publishEventMono)
                .expectNext(Boolean.TRUE)
                .verifyComplete();

        // Then: Ensure consumer processes the event and invokes expected methods
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(projectService, times(1)).findAll();
            verify(projectCapitalOptimizer, times(1)).maximizeCapital(any(CapitalMaximizationQuery.class));
        });
    }
}