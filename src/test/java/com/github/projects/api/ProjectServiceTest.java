package com.github.projects.api;

import com.github.projects.exception.InvalidProjectException;
import com.github.projects.exception.ProjectNotFoundException;
import com.github.projects.model.AuditMetadata;
import com.github.projects.model.ProjectDTO;
import com.github.projects.model.ProjectEntity;
import com.github.projects.model.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectCacheService projectCacheService;

    @InjectMocks
    private ProjectService underTest;

    private ProjectEntity projectEntity1;
    private ProjectEntity projectEntity2;

    @BeforeEach
    void setUp() {
        projectEntity1 = new ProjectEntity(randomUUID(), "Project 1", BigDecimal.ZERO, BigDecimal.ONE, AuditMetadata.empty(), 0L);
        projectEntity2 = new ProjectEntity(randomUUID(), "Project 2", BigDecimal.ONE, BigDecimal.TWO, AuditMetadata.empty(), 0L);
    }

    @Test
    void testAddAll_Success() {
        // Given
        Iterable<ProjectEntity> projects = List.of(projectEntity1, projectEntity2);
        when(projectRepository.saveAll(projects)).thenReturn(Flux.just(projectEntity1, projectEntity2));

        // When
        Flux<ProjectDTO> result = underTest.addAll(projects);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(projectDTO -> projectDTO.name().equals("Project 1"))
                .expectNextMatches(projectDTO -> projectDTO.name().equals("Project 2"))
                .verifyComplete();

        verify(projectRepository).saveAll(projects);
    }

    @Test
    void testAddAll_NullCollection() {
        // Given
        Iterable<ProjectEntity> nullProjects = null;

        // When
        Flux<ProjectDTO> result = underTest.addAll(nullProjects);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InvalidProjectException
                        && throwable.getMessage().equals("Project collection must not be null."))
                .verify();
    }

    @Test
    void testAddAll_EmptyCollection() {
        // Given
        Iterable<ProjectEntity> emptyProjects = Collections.emptyList();

        // When
        Flux<ProjectDTO> result = underTest.addAll(emptyProjects);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InvalidProjectException
                        && throwable.getMessage().equals("Project collection must not be empty or contain null elements."))
                .verify();
    }

    @Test
    void testFindById_ShouldReturnProjectFromRepository_WhenNotInCache() {
        // Given
        String projectId = "1";

        when(projectCacheService.getProjectFromCache(Mockito.anyString())).thenReturn(Mono.empty());
        when(projectRepository.findById(projectId)).thenReturn(Mono.just(projectEntity1));
        when(projectCacheService.cacheProject(Mockito.anyString(), Mockito.any())).thenReturn(Mono.just(true));

        // When
        Mono<ProjectDTO> result = underTest.findById(projectId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(projectDTO -> projectDTO.name().equals("Project 1"))
                .verifyComplete();

        Mockito.verify(projectCacheService).getProjectFromCache(projectId); // Attempt to read from cache (cache miss)
        Mockito.verify(projectRepository).findById(projectId);
        Mockito.verify(projectCacheService).cacheProject(Mockito.anyString(), Mockito.any());
    }

    @Test
    void testFindById_ProjectNotFoundException() {
        // Given
        String projectId = "2";

        when(projectCacheService.getProjectFromCache(Mockito.anyString())).thenReturn(Mono.empty());
        when(projectRepository.findById(projectId)).thenReturn(Mono.empty());

        // When
        Mono<ProjectDTO> result = underTest.findById(projectId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ProjectNotFoundException
                        && throwable.getMessage().contains("Project not found for ID: 2"))
                .verify();

        Mockito.verify(projectRepository).findById(projectId);
        Mockito.verify(projectCacheService, Mockito.never()).cacheProject(Mockito.anyString(), Mockito.any()); // No caching attempt was involved
    }

    @Test
    void testFindById_ShouldReturnCachedProject_WhenAvailable() {
        // Given
        String projectId = "1";

        ProjectDTO project = ProjectDTO.fromEntity(projectEntity1);
        when(projectCacheService.getProjectFromCache(projectId)).thenReturn(Mono.just(project));

        // When
        Mono<ProjectDTO> result = underTest.findById(projectId);

        StepVerifier.create(result)
                .expectNext(project)
                .verifyComplete();

        verify(projectRepository, never()).findById(Mockito.anyString()); // Read project from cache and call to db was never invoked
    }

    @Test
    void testFindById_ShouldThrowProjectNotFoundException_WhenProjectNotFoundInRepository() {
        // Given
        String projectId = "2";
        when(projectCacheService.getProjectFromCache(projectId)).thenReturn(Mono.empty());  // Cache miss
        when(projectRepository.findById(projectId)).thenReturn(Mono.empty());  // Repository miss

        // When
        Mono<ProjectDTO> result = underTest.findById(projectId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ProjectNotFoundException
                        && throwable.getMessage().equals("Project not found for ID: 2"))
                .verify();

        verify(projectCacheService).getProjectFromCache(projectId);
        verify(projectRepository).findById(projectId);
        verify(projectCacheService, never()).cacheProject(Mockito.anyString(), Mockito.any()); // No caching was invoked
    }

    @Test
    void testFindAll_success() {
        // Given
        when(projectRepository.findAll()).thenReturn(Flux.just(projectEntity1, projectEntity2));

        // When
        Flux<ProjectDTO> result = underTest.findAll();

        // Then
        StepVerifier.create(result)
                .expectNextMatches(project -> project.id().equals(projectEntity1.id()) &&
                        project.name().equals(projectEntity1.name()))
                .expectNextMatches(project -> project.id().equals(projectEntity2.id()) &&
                        project.name().equals(projectEntity2.name()))
                .verifyComplete();
    }
}