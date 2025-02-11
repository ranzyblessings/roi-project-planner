package com.github.projects.api;

import com.github.configuration.TestcontainersConfiguration;
import com.github.projects.model.AuditMetadata;
import com.github.projects.model.ProjectDTO;
import com.github.projects.model.ProjectEntity;
import com.github.projects.model.ProjectRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static com.github.configuration.CacheConfiguration.PROJECT_ID_CACHE_KEY;
import static java.util.UUID.randomUUID;

@Testcontainers
@SpringBootTest
class ProjectServiceCachingTest extends TestcontainersConfiguration {

    @MockitoBean
    private ProjectRepository projectRepository;

    private final CacheManager cacheManager;
    private final ProjectService projectService;

    private ProjectEntity projectEntity;

    @Autowired
    public ProjectServiceCachingTest(CacheManager cacheManager, ProjectService projectService) {
        this.cacheManager = cacheManager;
        this.projectService = projectService;
    }

    @BeforeEach
    void setUp() {
        final Cache usersCache = cacheManager.getCache(PROJECT_ID_CACHE_KEY);
        if (usersCache != null) {
            usersCache.clear();
        }

        projectEntity = new ProjectEntity(randomUUID(), "Project 1", new BigDecimal("100.00"),
                new BigDecimal("300"), AuditMetadata.empty(), 0L);
    }

    @Test
    void testFindById_shouldCacheResult() {
        Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Mono.just(projectEntity));

        // Given: A specific project ID
        final var projectID = "1";

        // When: Invoking the service method twice, expecting the result to be cached after the initial invocation
        ProjectDTO firstInvocationResult = projectService.findById(projectID).block();
        ProjectDTO secondInvocationResult = projectService.findById(projectID).block();

        // Then: Assert that both invocations return the same cached result, without querying the repository again.
        Assertions.assertThat(firstInvocationResult).isNotNull();
        Assertions.assertThat(secondInvocationResult).isNotNull();
        Assertions.assertThat(firstInvocationResult).isEqualTo(secondInvocationResult);

        // Verify that the repository was queried only once, confirming the caching mechanism.
        Mockito.verify(projectRepository, Mockito.times(1)).findById(Mockito.anyString());
    }
}