package com.github.projects.api;

import com.github.projects.model.ProjectDTO;
import com.github.projects.model.ProjectEntity;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/projects")
public class ProjectsApiController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectsApiController.class);

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
                .doOnNext(request -> logger.debug("Processing project: {}", request))
                .map(this::toProjectEntity)
                .collectList()
                .flatMap(projects -> {
                    if (projects.size() > 100) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create more than 100 projects at a time"));
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
}