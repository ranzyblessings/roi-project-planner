package com.github.analytics.api;

import com.github.analytics.event.CapitalMaximizationQueryEvent;
import com.github.analytics.event.ProjectCapitalOptimizerEventPublisher;
import com.github.projects.api.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/v1/capital/maximization/query")
public class ProjectCapitalOptimizerApiController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectCapitalOptimizerApiController.class);

    private final ProjectCapitalOptimizerEventPublisher projectCapitalOptimizerEventPublisher;

    public ProjectCapitalOptimizerApiController(ProjectCapitalOptimizerEventPublisher projectCapitalOptimizerEventPublisher) {
        this.projectCapitalOptimizerEventPublisher = projectCapitalOptimizerEventPublisher;
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping
    public Mono<ApiResponse<String>> publishCapitalMaximizationQueryEvent(
            @Valid @RequestBody Mono<ProjectCapitalOptimizerRequest> requestMono) {
        logger.info("Received request to publish capital maximization query event");

        return requestMono
                .flatMap(request -> {
                    logger.info("Project capital optimizer request: {}", request);

                    var event = new CapitalMaximizationQueryEvent(request.maxProjects(), request.initialCapital());
                    return projectCapitalOptimizerEventPublisher.publishEvent(event);
                })
                .thenReturn(ApiResponse.success(HttpStatus.ACCEPTED.value(), "Capital maximization query event accepted for processing"))
                .doOnError(error -> logger.error("Error publishing Capital Maximization Query event", error));
    }
}