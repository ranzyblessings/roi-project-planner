package com.github.analytics.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.analytics.api.CapitalMaximizationQuery;
import com.github.analytics.api.ProjectCapitalOptimized;
import com.github.analytics.api.ProjectCapitalOptimizer;
import com.github.projects.api.ProjectService;
import com.github.projects.model.AuditMetadata;
import com.github.projects.model.ProjectDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectCapitalOptimizerEventConsumerTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private ProjectCapitalOptimizer projectCapitalOptimizer;

    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private ProjectCapitalOptimizerEventConsumer underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessCapitalMaximizationEvent_successful() throws JsonProcessingException {
        // Given: a valid JSON event and a mock project service and project optimizer
        String jsonEvent = "{\"maxProjects\": 2, \"initialCapital\": \"100.00\"}";
        var event = new CapitalMaximizationQueryEvent(2, new BigDecimal("100.00"));

        when(jsonMapper.readValue(jsonEvent, CapitalMaximizationQueryEvent.class)).thenReturn(event);

        var project = new ProjectDTO(randomUUID(), "Project 1", new BigDecimal("100.00"), new BigDecimal("500.00"), AuditMetadata.empty(), 0L);
        when(projectService.findAll()).thenReturn(Flux.just(project));

        var optimized = new ProjectCapitalOptimized(List.of(project), new BigDecimal("500.00"));
        when(projectCapitalOptimizer.maximizeCapital(any(CapitalMaximizationQuery.class))).thenReturn(Mono.just(optimized));

        // When: the capital maximization event is processed
        Mono<ProjectCapitalOptimized> result = underTest.processCapitalMaximizationEvent(jsonEvent);

        // Then: the result should match the expected optimized project and final capital
        StepVerifier.create(result)
                .expectNextMatches(projectCapitalOptimized ->
                        projectCapitalOptimized.finalCapital().compareTo(new BigDecimal("500.00")) == 0 &&
                                projectCapitalOptimized.selectedProjects().contains(project))
                .verifyComplete();

        // Verify interactions: Ensure methods were called the expected number of times
        verify(projectService, times(1)).findAll();
        verify(projectCapitalOptimizer, times(1)).maximizeCapital(any(CapitalMaximizationQuery.class));

        // Verify no other interactions occurred
        verifyNoMoreInteractions(projectService, projectCapitalOptimizer);
    }

    @Test
    void testProcessCapitalMaximizationEvent_noProjectsFound() throws JsonProcessingException {
        // Given: a valid JSON event and a mock project service and project optimizer
        String jsonEvent = "{\"maxProjects\": 2, \"initialCapital\": \"100.00\"}";
        var event = new CapitalMaximizationQueryEvent(2, new BigDecimal("100.00"));

        when(jsonMapper.readValue(jsonEvent, CapitalMaximizationQueryEvent.class)).thenReturn(event);

        // Mock projectService to return an empty list (no projects available)
        when(projectService.findAll()).thenReturn(Flux.empty());

        // When: the capital maximization event is processed
        Mono<ProjectCapitalOptimized> result = underTest.processCapitalMaximizationEvent(jsonEvent);

        // Then: an exception should be thrown
        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify();

        // Verify interactions: Ensure methods were called the expected number of times
        verify(projectService, times(1)).findAll();
        verifyNoMoreInteractions(projectService, projectCapitalOptimizer);
    }
}