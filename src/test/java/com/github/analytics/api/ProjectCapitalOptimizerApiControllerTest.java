package com.github.analytics.api;

import com.github.analytics.event.CapitalMaximizationQueryEvent;
import com.github.analytics.event.ProjectCapitalOptimizerEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Objects;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(ProjectCapitalOptimizerApiController.class)
class ProjectCapitalOptimizerApiControllerTest {

    @MockitoBean
    private ProjectCapitalOptimizerEventPublisher projectCapitalOptimizerEventPublisher;

    private final WebTestClient webTestClient;

    @Autowired
    ProjectCapitalOptimizerApiControllerTest(WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    private static final String API_ENDPOINT = "/api/v1/capital/maximization/query";

    @Test
    void shouldAcceptCapitalMaximizationQueryEvent_WhenRequestIsValid() {
        // Given
        var request = validRequest();
        var expectedEvent = new CapitalMaximizationQueryEvent(request.maxProjects(), request.initialCapital());

        when(projectCapitalOptimizerEventPublisher.publishEvent(any(CapitalMaximizationQueryEvent.class)))
                .thenReturn(Mono.empty());

        // When & Then
        webTestClient.post()
                .uri(API_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(HttpStatus.ACCEPTED.value())
                .jsonPath("$.data").isEqualTo("Capital Maximization Query event accepted for processing");

        verify(projectCapitalOptimizerEventPublisher, times(1))
                .publishEvent(argThat(event ->
                        Objects.equals(event.maxProjects(), expectedEvent.maxProjects()) &&
                                event.initialCapital().compareTo(expectedEvent.initialCapital()) == 0
                ));

        verifyNoMoreInteractions(projectCapitalOptimizerEventPublisher);
    }

    @Test
    void shouldReturnInternalServerError_WhenEventPublishingFails() {
        // Given
        var request = validRequest();

        when(projectCapitalOptimizerEventPublisher.publishEvent(any(CapitalMaximizationQueryEvent.class)))
                .thenReturn(Mono.error(new RuntimeException("Publishing failed")));

        // When & Then
        webTestClient.post()
                .uri(API_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .jsonPath("$.message").value(containsString("Publishing failed"));

        verify(projectCapitalOptimizerEventPublisher, times(1)).publishEvent(any(CapitalMaximizationQueryEvent.class));
        verifyNoMoreInteractions(projectCapitalOptimizerEventPublisher);
    }

    @Test
    void shouldReturnBadRequest_WhenMaxProjectsIsNull() {
        // Given
        var invalidRequest = new ProjectCapitalOptimizerRequest(null, new BigDecimal("100.00"));

        // When & Then
        webTestClient.post()
                .uri(API_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").value(containsString("Maximum projects cannot be null"));
    }

    @Test
    void shouldReturnBadRequest_WhenMaxProjectsIsNegative() {
        // Given
        var invalidRequest = new ProjectCapitalOptimizerRequest(-1, new BigDecimal("100.00"));

        // When & Then
        webTestClient.post()
                .uri(API_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").value(containsString("Maximum projects must be at least 1"));
    }

    @Test
    void shouldReturnBadRequest_WhenInitialCapitalIsNull() {
        // Given
        var invalidRequest = new ProjectCapitalOptimizerRequest(2, null);

        // When & Then
        webTestClient.post()
                .uri(API_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").value(containsString("Initial capital cannot be null"));
    }

    @Test
    void shouldReturnBadRequest_WhenInitialCapitalIsNegative() {
        // Given
        var invalidRequest = new ProjectCapitalOptimizerRequest(2, new BigDecimal("-10.00"));

        // When & Then
        webTestClient.post()
                .uri(API_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").value(containsString("Initial capital cannot be negative"));
    }

    private ProjectCapitalOptimizerRequest validRequest() {
        return new ProjectCapitalOptimizerRequest(2, new BigDecimal("100.00"));
    }
}