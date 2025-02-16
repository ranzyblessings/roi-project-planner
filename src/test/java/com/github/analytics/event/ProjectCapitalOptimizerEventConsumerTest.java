package com.github.analytics.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.analytics.api.CapitalMaximizationQuery;
import com.github.analytics.api.ProjectCapitalOptimized;
import com.github.analytics.api.ProjectCapitalOptimizer;
import com.github.projects.api.ProjectService;
import com.github.projects.model.AuditMetadata;
import com.github.projects.model.ProjectDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectCapitalOptimizerEventConsumerTest {

    @Mock
    private ProjectService projectService;
    @Mock
    private ProjectCapitalOptimizer projectCapitalOptimizer;

    @InjectMocks
    private ProjectCapitalOptimizerEventConsumer underTest;

    private static final String VALID_JSON_EVENT = "{\"maxProjects\": 2, \"initialCapital\": \"100.00\"}";

    @Test
    void shouldProcessCapitalMaximizationEventSuccessfully() throws JsonProcessingException {
        // Given
        var project = new ProjectDTO(randomUUID(), "Project 1", new BigDecimal("100.00"), new BigDecimal("500.00"), AuditMetadata.empty(), 0L);
        var optimized = new ProjectCapitalOptimized(List.of(project), new BigDecimal("500.00"));

        when(projectService.findAll()).thenReturn(Flux.just(project));
        when(projectCapitalOptimizer.maximizeCapital(any(CapitalMaximizationQuery.class))).thenReturn(Mono.just(optimized));

        // When
        Mono<ProjectCapitalOptimized> result = underTest.processCapitalMaximizationEvent(VALID_JSON_EVENT);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(res ->
                        res.finalCapital().compareTo(new BigDecimal("500.00")) == 0 &&
                                res.selectedProjects().contains(project))
                .verifyComplete();

        verify(projectService, times(1)).findAll();
        verify(projectCapitalOptimizer, times(1)).maximizeCapital(any(CapitalMaximizationQuery.class));
        verifyNoMoreInteractions(projectService, projectCapitalOptimizer);
    }

    @Test
    void shouldThrowErrorWhenNoProjectsFound() throws JsonProcessingException {
        // Given
        when(projectService.findAll()).thenReturn(Flux.empty());

        // When
        Mono<ProjectCapitalOptimized> result = underTest.processCapitalMaximizationEvent(VALID_JSON_EVENT);

        // Then
        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(IllegalStateException.class);
                    assertThat(ex.getMessage()).contains("No projects available for capital maximization.");
                })
                .verify();

        verify(projectService, times(1)).findAll();
        verifyNoMoreInteractions(projectService, projectCapitalOptimizer);
    }

    @Test
    void shouldThrowErrorWhenJsonDeserializationFails() {
        // Given
        String invalidJson = "INVALID_JSON";

        // When
        Mono<ProjectCapitalOptimized> result = underTest.processCapitalMaximizationEvent(invalidJson);

        // Then
        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(IllegalArgumentException.class);
                    assertThat(ex.getMessage()).contains("Invalid JSON format");
                })
                .verify();

        verifyNoInteractions(projectService, projectCapitalOptimizer);
    }
}