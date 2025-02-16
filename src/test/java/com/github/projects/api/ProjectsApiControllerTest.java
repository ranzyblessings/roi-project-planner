package com.github.projects.api;

import com.github.projects.model.AuditMetadata;
import com.github.projects.model.ProjectDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@WebFluxTest(ProjectsApiController.class)
class ProjectsApiControllerTest {

    @MockitoBean
    private ProjectService projectService;

    private final WebTestClient webTestClient;

    @Autowired
    ProjectsApiControllerTest(WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    @Test
    void testCreateNewProjects_Success() {
        // Given
        var request1 = new CreateProjectRequest("Project 1", new BigDecimal("100.00"), new BigDecimal("500.00"));
        var request2 = new CreateProjectRequest("Project 2", new BigDecimal("150.00"), new BigDecimal("800.00"));

        Flux<CreateProjectRequest> requestFlux = Flux.just(request1, request2);

        var projectDTO1 = new ProjectDTO(randomUUID(), "Project 1", new BigDecimal("100.00"), new BigDecimal("500.00"), AuditMetadata.empty(), 0L);
        var projectDTO2 = new ProjectDTO(randomUUID(), "Project 2", new BigDecimal("150.00"), new BigDecimal("800.00"), AuditMetadata.empty(), 0L);

        when(projectService.addAll(anyList())).thenReturn(Flux.just(projectDTO1, projectDTO2));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/projects")
                .body(requestFlux, Flux.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(201)
                .jsonPath("$.data[0].id").isNotEmpty()
                .jsonPath("$.data[0].name").isEqualTo("Project 1")
                .jsonPath("$.data[0].requiredCapital").isEqualTo(100.00)
                .jsonPath("$.data.[0].profit").isEqualTo(500.00)
                .jsonPath("$.data.[1].id").isNotEmpty()
                .jsonPath("$.data.[1].name").isEqualTo("Project 2")
                .jsonPath("$.data.[1].requiredCapital").isEqualTo(150.00)
                .jsonPath("$.data.[1].profit").isEqualTo(800.00);
    }

    @Test
    void testCreateNewProjects_TooManyProjects_ShouldReturnBadRequest() {
        // Given
        List<CreateProjectRequest> tooManyRequests = IntStream.range(0, 101)
                .mapToObj(i -> new CreateProjectRequest("Project %d".formatted(i), new BigDecimal("100.00"),
                        new BigDecimal("500.00")))
                .toList();

        Flux<CreateProjectRequest> requestFlux = Flux.fromIterable(tooManyRequests);

        // When & Then
        webTestClient.post()
                .uri("/api/v1/projects")
                .body(requestFlux, Flux.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Cannot create more than 100 projects at a time");
    }

    @Test
    void testCreateNewProjects_InvalidRequest_ShouldReturnBadRequest() {
        // Given
        var invalidRequest = new CreateProjectRequest("", null, new BigDecimal("-10.00"));

        Flux<CreateProjectRequest> requestFlux = Flux.just(invalidRequest);

        // When & Then
        webTestClient.post()
                .uri("/api/v1/projects")
                .body(requestFlux, Flux.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(400)
                .jsonPath("$.message").exists(); // Validation error messages should be returned
    }
}