package com.github.projects.api;

import com.github.projects.exception.InvalidProjectException;
import com.github.projects.exception.ProjectNotFoundException;
import com.github.projects.model.ProjectDTO;
import com.github.projects.model.ProjectEntity;
import com.github.projects.model.ProjectRepository;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;
import java.util.stream.StreamSupport;

@Service
public class ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private static final String PROJECTS_RESILIENCE_CONFIG_NAME = "projects";

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
     * Retrieves a project by ID with caching and resilience mechanisms.
     * Falls back to cache on timeouts or circuit breaker activation.
     */
    @TimeLimiter(name = PROJECTS_RESILIENCE_CONFIG_NAME, fallbackMethod = "findByIdTimeoutFallback")
    @CircuitBreaker(name = PROJECTS_RESILIENCE_CONFIG_NAME, fallbackMethod = "findByIdCircuitBreakerFallback")
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

    private Mono<ProjectDTO> findByIdTimeoutFallback(String id, TimeoutException e) {
        logger.info("Project findById request timed out after 2 seconds. Checking cache for record as fallback.", e);
        return projectCacheService.getProjectFromCache(id).switchIfEmpty(Mono.empty());
    }

    private Mono<ProjectDTO> findByIdCircuitBreakerFallback(String id, CallNotPermittedException e) {
        logger.info("Project findById encountered an error. Checking cache for record as fallback.", e);
        return projectCacheService.getProjectFromCache(id).switchIfEmpty(Mono.empty());
    }

    /**
     * Retrieves all projects from the repository as a reactive stream.
     */
    public Flux<ProjectDTO> findAll() {
        return projectRepository.findAll().map(ProjectDTO::fromEntity);
    }
}