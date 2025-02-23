package com.github.projects.api;

import com.github.configuration.CacheConfiguration;
import com.github.projects.model.ProjectDTO;
import com.github.projects.model.Validators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProjectCacheService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectCacheService.class);

    private final ReactiveValueOperations<String, ProjectDTO> valueOps;

    public ProjectCacheService(ReactiveRedisTemplate<String, ProjectDTO> redisTemplate) {
        this.valueOps = redisTemplate.opsForValue();
    }

    /**
     * Caches a project with the given ID.
     */
    public Mono<Boolean> cacheProject(String id, ProjectDTO project) {
        Validators.requireNonNullOrBlank(id, () -> "project ID should not be null or empty");
        Validators.requireNonNull(project, () -> "project should not be");

        final String cacheKey = createCacheKey(id);
        logger.info("Caching project with ID '{}' in Redis.", cacheKey);

        return valueOps.set(cacheKey, project)
                .doOnTerminate(() -> logger.info("Caching completed for project ID '{}'.", cacheKey))
                .onErrorResume(error -> {
                    logger.error("Error caching project with ID '{}': {}", cacheKey, error.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Retrieves a project from the cache using its ID.
     */
    public Mono<ProjectDTO> getProjectFromCache(String id) {
        Validators.requireNonNullOrBlank(id, () -> "project ID should not be null or empty");

        final String cacheKey = createCacheKey(id);
        logger.info("Fetching project with ID '{}' from cache.", cacheKey);

        return valueOps.get(cacheKey)
                .doOnTerminate(() -> logger.info("Cache retrieval completed for ID: '{}'", cacheKey))
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Project with ID '{}' not found in cache.", cacheKey);
                    return Mono.empty();
                }));
    }

    /**
     * Evicts a project from the cache using its ID.
     */
    public Mono<Boolean> evictCache(String id) {
        Validators.requireNonNullOrBlank(id, () -> "project ID should not be null or empty");

        final String cacheKey = createCacheKey(id);
        logger.info("Evicting project with ID '{}' from cache.", cacheKey);

        return valueOps.delete(cacheKey)
                .doOnTerminate(() -> logger.info("Cache eviction completed for ID '{}'.", cacheKey))
                .onErrorResume(error -> {
                    logger.error("Error evicting project with ID '{}': {}", cacheKey, error.getMessage());
                    return Mono.just(false); // Returning false if eviction fails
                });
    }

    private String createCacheKey(final String projectId) {
        return "%s:%s".formatted(CacheConfiguration.PROJECT_ID_CACHE_KEY, projectId);
    }
}