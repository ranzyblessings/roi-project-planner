package com.github.projects.api;

import com.github.configuration.TestcontainersConfiguration;
import com.github.projects.model.AuditMetadata;
import com.github.projects.model.ProjectDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;

import static java.util.UUID.randomUUID;

@SpringBootTest
class ProjectCacheServiceTest extends TestcontainersConfiguration {
    private static final int REDIS_PORT = 6379;

    private final ProjectCacheService underTest;

    @Autowired
    public ProjectCacheServiceTest(ProjectCacheService underTest) {
        this.underTest = underTest;
    }

    @Container
    private static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("redis:7.4.2"))
                    .withExposedPorts(REDIS_PORT)
                    .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(REDIS_PORT));
    }

    @Test
    void shouldCacheProjectWhenValidProjectIsProvided() {
        ProjectDTO testProject = createTestProject();

        Mono<Boolean> cacheResult = underTest.cacheProject(testProject.id().toString(), testProject);

        StepVerifier.create(cacheResult)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldReturnProjectWhenPresentInCache() {
        ProjectDTO testProject = createTestProject();
        underTest.cacheProject(testProject.id().toString(), testProject).block(); // wait for caching to complete

        Mono<ProjectDTO> cachedProject = underTest.getProjectFromCache(testProject.id().toString());

        StepVerifier.create(cachedProject)
                .expectNext(testProject)
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyMonoWhenProjectNotFoundInCache() {
        ProjectDTO testProject = createTestProject();

        Mono<ProjectDTO> cachedProject = underTest.getProjectFromCache(testProject.id().toString());

        StepVerifier.create(cachedProject).verifyComplete(); // No result
    }

    @Test
    void shouldEvictProjectFromCacheSuccessfully() {
        ProjectDTO testProject = createTestProject();
        String projectId = testProject.id().toString();
        underTest.cacheProject(projectId, testProject).block();

        Mono<Boolean> evictionResult = underTest.evictCache(projectId);
        StepVerifier.create(evictionResult)
                .expectNext(true)
                .verifyComplete();

        Mono<ProjectDTO> retrievedProject = underTest.getProjectFromCache(projectId);
        StepVerifier.create(retrievedProject).verifyComplete(); // No result after eviction
    }

    private static ProjectDTO createTestProject() {
        return new ProjectDTO(randomUUID(),
                "Project 1", new BigDecimal("100"), new BigDecimal("500"),
                new AuditMetadata(Instant.now(), Instant.now()), 0L
        );
    }
}