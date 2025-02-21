package com.github.analytics.api;

import com.github.analytics.exception.InvalidCapitalMaximizationQueryException;
import com.github.projects.model.AuditMetadata;
import com.github.projects.model.ProjectDTO;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ProjectCapitalOptimizerTest {
    private final ProjectCapitalOptimizer underTest = new ProjectCapitalOptimizer();

    @Test
    void shouldReturnError_whenQueryIsNull() {
        // Given: A null query passed to maximizeCapital method
        CapitalMaximizationQuery query = null;

        // When: maximizeCapital is called with the null query
        Mono<ProjectCapitalOptimized> resultMono = underTest.maximizeCapital(query);

        // Then: Verify that an IllegalArgumentException is thrown with the correct message
        StepVerifier.create(resultMono)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(InvalidCapitalMaximizationQueryException.class);
                    assertThat(error).hasMessage("Query must not be null.");
                })
                .verify();
    }

    @Test
    void shouldReturnInitialCapitalWhenNoProjectsAreAffordable() {
        // Given: Projects with required capital higher than the initial capital.
        List<ProjectDTO> projects = List.of(
                new ProjectDTO(randomUUID(), "Project X", new BigDecimal("10"), new BigDecimal("5"), AuditMetadata.empty(), 0L),
                new ProjectDTO(randomUUID(), "Project Y", new BigDecimal("20"), new BigDecimal("10"), AuditMetadata.empty(), 0L)
        );
        CapitalMaximizationQuery query = new CapitalMaximizationQuery(projects, 3, BigDecimal.ZERO);

        // When: We maximize the capital using the optimizer.
        Mono<ProjectCapitalOptimized> resultMono = underTest.maximizeCapital(query);

        // Then: The final capital should remain as the initial capital and no projects should be selected.
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result.finalCapital())
                            .as("Final Capital remains unchanged")
                            .isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(result.selectedProjects())
                            .as("No Projects Selected")
                            .isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void shouldMaximizeCapitalWhenProjectsAreAvailable() {
        // Given: A set of projects where some projects become affordable sequentially.
        List<ProjectDTO> projects = List.of(
                new ProjectDTO(randomUUID(), "Project A", BigDecimal.ZERO, BigDecimal.ONE, AuditMetadata.empty(), 0L),
                new ProjectDTO(randomUUID(), "Project B", BigDecimal.ONE, new BigDecimal("2"), AuditMetadata.empty(), 0L),
                new ProjectDTO(randomUUID(), "Project C", BigDecimal.ONE, new BigDecimal("3"), AuditMetadata.empty(), 0L)
        );
        CapitalMaximizationQuery query = new CapitalMaximizationQuery(projects, 2, BigDecimal.ZERO);

        // When: We maximize the capital using the optimizer.
        Mono<ProjectCapitalOptimized> resultMono = underTest.maximizeCapital(query);

        // Then: The final capital should be 4 (0 + 1 + 3) and selected projects should be as expected.
        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertThat(result.finalCapital())
                            .as("Final Capital")
                            .isEqualByComparingTo(new BigDecimal("4"));

                    List<String> selectedProjectNames = result.selectedProjects()
                            .stream()
                            .map(ProjectDTO::name)
                            .toList();
                    assertThat(selectedProjectNames)
                            .as("Selected Projects")
                            .containsExactly("Project A", "Project C");
                })
                .verifyComplete();
    }
}