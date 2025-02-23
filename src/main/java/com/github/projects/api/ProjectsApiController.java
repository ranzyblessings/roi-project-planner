package com.github.projects.api;

import com.github.projects.exception.TooManyProjectsException;
import com.github.projects.model.ProjectDTO;
import com.github.projects.model.ProjectEntity;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/projects")
public class ProjectsApiController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectsApiController.class);
    private static final int PROJECT_CREATION_LIMIT = 100;

    private final ProjectService projectService;

    public ProjectsApiController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<ApiResponse<List<ProjectDTO>>> createProjects(
            @Valid @RequestBody Flux<CreateProjectRequest> requestFlux) {
        logger.info("Received request to create new projects");

        return requestFlux
                .doOnNext(request -> logger.info("Processing project: {}", request))
                .map(this::toProjectEntity)
                .collectList()
                .flatMap(projects -> {
                    if (projects.size() > PROJECT_CREATION_LIMIT) {
                        return Mono.error(new TooManyProjectsException("Cannot create more than 100 projects at a time"));
                    }
                    return saveProjects(projects);
                })
                .doOnError(error -> logger.error("Error occurred while creating projects", error));
    }

    private ProjectEntity toProjectEntity(CreateProjectRequest request) {
        return ProjectEntity.createNewProject(request.name(), request.requiredCapital(), request.profit());
    }

    private Mono<ApiResponse<List<ProjectDTO>>> saveProjects(List<ProjectEntity> projects) {
        logger.info("Saving {} projects", projects.size());

        return projectService.addAll(projects)
                .collectList()
                .map(result -> {
                    var response = ApiResponse.success(HttpStatus.CREATED.value(), result);
                    logger.info("Successfully created {} projects: ApiResponse(statusCode={}, data={})",
                            result.size(), response.getStatusCode(), response.getData());
                    return response;
                });
    }

    // TODO: Implement pagination to avoid loading large datasets entirely into memory and improve scalability.
    @GetMapping
    public Mono<ApiResponse<List<ProjectDTO>>> listAllProjects() {
        logger.info("Fetching all projects from the database...");

        return projectService.findAll()
                .collectList()
                .doOnTerminate(() -> logger.info("Completed fetching all projects."))
                .map(projects -> {
                    logger.debug("Returning {} projects.", projects.size());
                    return ApiResponse.success(HttpStatus.OK.value(), projects);
                })
                .doOnError(error -> logger.error("Error fetching all projects", error));
    }

    @GetMapping(value = "/{id}")
    public Mono<ApiResponse<ProjectDTO>> listProjectById(@PathVariable String id) {
        logger.info("Fetching project with ID: {}", id);

        return projectService.findById(id)
                .map(project -> {
                    logger.debug("Found project with ID: {}", id);
                    return ApiResponse.success(HttpStatus.OK.value(), project);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Project with ID '{}' not found.", id);
                    return Mono.just(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Project not found for ID: %s".formatted(id)));
                }))
                .doOnError(error -> logger.error("Error fetching project with ID '{}'", id, error));
    }
}