package com.github.projects.api;

import com.github.projects.model.ProjectDTO;
import com.github.projects.model.ProjectEntity;
import com.github.projects.model.ProjectRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.NoSuchElementException;

import static com.github.configuration.CacheConfiguration.PROJECT_ID_CACHE_KEY;
import static com.github.projects.model.Validators.requireNonNullAndNoNullElements;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Asynchronously saves a collection of projects to the repository while ensuring that no null or empty elements are included.
     */
    public Flux<ProjectDTO> addAll(Iterable<ProjectEntity> projects) {
        return Mono.fromCallable(() -> {
                    requireNonNullAndNoNullElements((Collection<ProjectEntity>) projects, () -> "Projects cannot be null or empty");
                    return projects;
                })
                .flatMapMany(projectRepository::saveAll)
                .map(ProjectDTO::fromEntity);
    }

    /**
     * Retrieves a project by its ID from the repository.
     * Caches the result to improve performance for repeated lookups of the same project ID.
     */
    @Cacheable(value = PROJECT_ID_CACHE_KEY, key = "#id")
    public Mono<ProjectDTO> findById(String id) {
        return projectRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("Project not found for ID: %s".formatted(id))))
                .map(ProjectDTO::fromEntity);
    }

    /**
     * Retrieves all projects from the repository as a stream of ProjectDTO objects.
     */
    public Flux<ProjectDTO> findAll() {
        return projectRepository.findAll().map(ProjectDTO::fromEntity);
    }
}