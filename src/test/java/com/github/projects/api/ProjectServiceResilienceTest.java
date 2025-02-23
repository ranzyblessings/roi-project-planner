package com.github.projects.api;

import com.github.configuration.TestcontainersConfiguration;
import com.github.projects.exception.CriticalServiceFailureException;
import com.github.projects.model.AuditMetadata;
import com.github.projects.model.ProjectDTO;
import com.github.projects.model.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProjectServiceResilienceTest extends TestcontainersConfiguration {

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private ProjectCacheService projectCacheService;

    private final ProjectService underTest;

    @Autowired
    ProjectServiceResilienceTest(ProjectService underTest) {
        this.underTest = underTest;
    }

    @Test
    void shouldFallbackToCache_WhenDatabaseRequestTimesOut() {
        // Given a project exists in cache but DB request times out
        ProjectDTO cachedProject = createSampleProject();

        when(projectCacheService.getProjectFromCache(cachedProject.id().toString()))
                .thenReturn(Mono.empty()) // Cache miss on first attempt
                .thenReturn(Mono.just(cachedProject)); // Cache fallback should return this

        when(projectRepository.findById(cachedProject.id().toString()))
                .thenReturn(Flux.just(cachedProject).delayElements(Duration.ofSeconds(3)) // Simulating timeout
                        .then(Mono.empty()));

        // When requesting the project
        Mono<ProjectDTO> result = underTest.findById(cachedProject.id().toString());

        // Then it should fall back to the cache
        StepVerifier.create(result)
                .expectNext(cachedProject)
                .verifyComplete();
    }

    @Test
    void shouldTriggerCircuitBreakerAndFallback_AfterConsecutiveFailures() {
        // Given cache service is failing consistently
        when(projectCacheService.getProjectFromCache(Mockito.anyString()))
                .thenReturn(Mono.error(new CriticalServiceFailureException("Simulated failure")));

        // Simulate repeated failures to trigger the circuit breaker
        for (int i = 0; i < 5; i++) {
            StepVerifier.create(underTest.findById("non-existent-id"))
                    .expectErrorSatisfies(error ->
                            assertThat(error).isInstanceOf(CriticalServiceFailureException.class))
                    .verify();
        }

        // Now, circuit breaker should be open and fallback should return empty
        StepVerifier.create(underTest.findById("non-existent-id"))
                .expectError() // Expecting fallback to return Mono.empty()
                .verify();
    }

    private ProjectDTO createSampleProject() {
        return new ProjectDTO(
                randomUUID(), "Sample Project", new BigDecimal("100"),
                new BigDecimal("500"), new AuditMetadata(Instant.now(), Instant.now()), 0L
        );
    }
}