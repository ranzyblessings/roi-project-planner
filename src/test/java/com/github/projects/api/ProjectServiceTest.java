package com.github.projects.api;

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
import java.util.NoSuchElementException;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService underTest;

    private ProjectEntity projectEntity1;
    private ProjectEntity projectEntity2;

    @BeforeEach
    void setUp() {
        // Set up sample project entities
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
    void testAddAll_EmptyCollection() {
        // Given
        Iterable<ProjectEntity> emptyProjects = Collections.emptyList();

        // When
        Flux<ProjectDTO> result = underTest.addAll(emptyProjects);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().equals("Projects cannot be null or empty"))
                .verify();
    }

    @Test
    void testAddAll_NullCollection() {
        // Given
        Iterable<ProjectEntity> nullProjects = null;

        // When
        Flux<ProjectDTO> result = underTest.addAll(nullProjects);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NullPointerException
                        && throwable.getMessage().equals("Projects cannot be null or empty"))
                .verify();
    }

    @Test
    void testFindById_ProjectFound() {
        // Given
        String projectId = "1";

        when(projectRepository.findById(projectId)).thenReturn(Mono.just(projectEntity1));

        // When
        Mono<ProjectDTO> result = underTest.findById(projectId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(projectDTO -> projectDTO.name().equals("Project 1"))
                .verifyComplete();

        Mockito.verify(projectRepository).findById(projectId);
    }

    @Test
    void testFindById_ProjectNotFound() {
        // Given
        String projectId = "2";

        when(projectRepository.findById(projectId)).thenReturn(Mono.empty());

        // When
        Mono<ProjectDTO> result = underTest.findById(projectId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NoSuchElementException
                        && throwable.getMessage().contains("Project not found for ID: 2"))
                .verify();

        Mockito.verify(projectRepository).findById(projectId);
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