package com.github.projects.api;

import com.github.projects.exception.InvalidProjectException;
import com.github.projects.exception.ProjectNotFoundException;
import com.github.projects.model.ProjectDTO;
import com.github.projects.model.ProjectEntity;
import com.github.projects.model.ProjectRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.StreamSupport;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectCacheService projectCacheService;

    public ProjectService(ProjectRepository projectRepository, ProjectCacheService projectCacheService) {
        this.projectRepository = projectRepository;
        this.projectCacheService = projectCacheService;
    }

    /**
     * Lazily and asynchronously saves a collection of projects to the repository, ensuring that the input is valid.
     */
    public Flux<ProjectDTO> addAll(final Iterable<ProjectEntity> projects) {
        return Flux.defer(() -> {
            if (projects == null) {
                return Flux.error(new InvalidProjectException("Project collection must not be null."));
            }

            final var projectList = StreamSupport.stream(projects.spliterator(), false).toList();

            if (projectList.isEmpty() || projectList.contains(null)) {
                return Flux.error(new InvalidProjectException("Project collection must not be empty or contain null elements."));
            }

            return projectRepository.saveAll(projectList).map(ProjectDTO::fromEntity);
        });
    }

    /**
     * Retrieves a project by its ID from the repository.
     * Caches the result to optimize performance for repeated lookups of the same project ID.
     */
    public Mono<ProjectDTO> findById(final String id) {
        return projectCacheService.getProjectFromCache(id)
                .switchIfEmpty(
                        Mono.defer(() -> projectRepository.findById(id)
                                .switchIfEmpty(Mono.error(new ProjectNotFoundException("Project not found for ID: %s".formatted(id))))
                                .map(ProjectDTO::fromEntity)
                                .flatMap(project -> projectCacheService.cacheProject(id, project).thenReturn(project))
                        )
                );
    }

    /**
     * Retrieves all projects from the repository as a reactive stream.
     */
    public Flux<ProjectDTO> findAll() {
        return projectRepository.findAll().map(ProjectDTO::fromEntity);
    }
}