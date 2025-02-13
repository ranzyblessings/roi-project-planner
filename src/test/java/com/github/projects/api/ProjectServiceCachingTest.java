package com.github.projects.api;

import com.github.configuration.TestcontainersConfiguration;
import com.github.projects.model.AuditMetadata;
import com.github.projects.model.ProjectDTO;
import com.github.projects.model.ProjectEntity;
import com.github.projects.model.ProjectRepository;
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
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static com.github.configuration.CacheConfiguration.PROJECT_ID_CACHE_KEY;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

@Testcontainers
@SpringBootTest
class ProjectServiceCachingTest extends TestcontainersConfiguration {

    @MockitoBean
    private ProjectRepository projectRepository;

    private final ProjectService underTest;
    private final CacheManager cacheManager;

    @Autowired
    public ProjectServiceCachingTest(ProjectService underTest, CacheManager cacheManager) {
        this.underTest = underTest;
        this.cacheManager = cacheManager;
    }

    private ProjectEntity projectEntity;

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
        Mockito.when(projectRepository.findById(anyString())).thenReturn(Mono.just(projectEntity));

        // Given: A request is made to find a project by its ID
        final var projectID = "1";

        // When: The first call to findById should return the mapped ProjectDTO
        StepVerifier.create(underTest.findById(projectID))
                .expectNext(ProjectDTO.fromEntity(projectEntity))
                .verifyComplete();

        // When: The second call should return the cached result
        StepVerifier.create(underTest.findById(projectID))
                .expectNext(ProjectDTO.fromEntity(projectEntity)) // Expect the same result from cache
                .verifyComplete();

        // Then: Verify that the repository's findById method was only called once
        // confirming that the result is being cached.
        Mockito.verify(projectRepository, times(1)).findById(anyString());
    }
}