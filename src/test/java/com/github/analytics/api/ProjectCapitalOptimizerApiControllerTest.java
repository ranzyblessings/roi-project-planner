package com.github.analytics.api;

import com.github.analytics.event.CapitalMaximizationQueryEvent;
import com.github.analytics.event.ProjectCapitalOptimizerEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebFluxTest(ProjectCapitalOptimizerApiController.class)
class ProjectCapitalOptimizerApiControllerTest {

    @MockitoBean
    private ProjectCapitalOptimizerEventPublisher projectCapitalOptimizerEventPublisher;

    @InjectMocks
    private ProjectCapitalOptimizerApiController underTest;

    private final WebTestClient webTestClient;

    @Autowired
    ProjectCapitalOptimizerApiControllerTest(WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    @Test
    void testPublishCapitalMaximizationQueryEvent_successful() {
        // Given
        var request = new ProjectCapitalOptimizerRequest(2, new BigDecimal("100.00"));
        var event = new CapitalMaximizationQueryEvent(2, new BigDecimal("100.00"));

        // Mock the behavior of the publisher to return a successful event publishing
        when(projectCapitalOptimizerEventPublisher.publishEvent(any(CapitalMaximizationQueryEvent.class)))
                .thenReturn(Mono.just(true)); // Simulate successful event publishing

        // When & Then
        webTestClient.post()
                .uri("/api/v1/capital/maximization/query")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isAccepted()  // Expect HTTP 202 ACCEPTED
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(HttpStatus.ACCEPTED.value())
                .jsonPath("$.data").isEqualTo("Capital maximization query event accepted for processing.");

        // Verify that the event was published
        verify(projectCapitalOptimizerEventPublisher, times(1)).publishEvent(eq(event));
    }

    @Test
    void testPublishCapitalMaximizationQueryEvent_failed() {
        // Given
        var request = new ProjectCapitalOptimizerRequest(2, new BigDecimal("100.00"));
        var event = new CapitalMaximizationQueryEvent(2, new BigDecimal("100.00"));

        // Mock the behavior of the publisher to return an error (simulate failure)
        when(projectCapitalOptimizerEventPublisher.publishEvent(any(CapitalMaximizationQueryEvent.class)))
                .thenReturn(Mono.error(new RuntimeException("Error occurred while publishing capital maximization query event.")));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/capital/maximization/query")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.statusCode").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .jsonPath("$.message").isEqualTo("Error occurred while publishing capital maximization query event.");

        // Verify that the event publishing attempt was made
        verify(projectCapitalOptimizerEventPublisher, times(1)).publishEvent(eq(event));
    }
}