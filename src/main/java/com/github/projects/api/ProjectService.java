package com.github.projects.api;

import com.github.projects.exception.InvalidProjectException;
import com.github.projects.exception.ProjectNotFoundException;
import com.github.projects.model.ProjectDTO;
import com.github.projects.model.ProjectEntity;
import com.github.projects.model.ProjectRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.StreamSupport;

import static com.github.configuration.CacheConfiguration.PROJECT_ID_CACHE_KEY;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Lazily asynchronously saves a collection of projects to the repository while ensuring the input is valid.
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
     * Caches the result to improve performance for repeated lookups of the same project ID.
     */
    @Cacheable(value = PROJECT_ID_CACHE_KEY, key = "#id")
    public Mono<ProjectDTO> findById(final String id) {
        return projectRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProjectNotFoundException("Project not found for ID: %s".formatted(id))))
                .map(ProjectDTO::fromEntity);
    }

    /**
     * Retrieves all projects from the repository as a stream of ProjectDTO objects.
     */
    public Flux<ProjectDTO> findAll() {
        return projectRepository.findAll().map(ProjectDTO::fromEntity);
    }
}