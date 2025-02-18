package com.github.analytics.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectCapitalOptimizerEventPublisherTest {
    private static final String CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING = "capital-maximization-query-out-0";
    private static final String PARTITION_KEY_HEADER = "PARTITION_KEY";

    @Mock
    private StreamBridge streamBridge;

    @InjectMocks
    private ProjectCapitalOptimizerEventPublisher publisher;

    @Test
    void shouldPublishEventSuccessfully_WhenStreamBridgeReturnsTrue() {
        // Given
        CapitalMaximizationQueryEvent event = new CapitalMaximizationQueryEvent(2, new BigDecimal("100.00"));
        String expectedPartitionKey = computePartitionKey(event);

        // Ensure the stub matches the exact parameters
        when(streamBridge.send(eq(CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING), any(Message.class)))
                .thenReturn(true);

        // When
        Mono<Boolean> result = publisher.publishEvent(event);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        ArgumentCaptor<Message<CapitalMaximizationQueryEvent>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(streamBridge, times(1))
                .send(eq(CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING), messageCaptor.capture());

        Message<CapitalMaximizationQueryEvent> actualMessage = messageCaptor.getValue();
        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage.getPayload()).isEqualTo(event);
        assertThat(actualMessage.getHeaders()).containsEntry(PARTITION_KEY_HEADER, expectedPartitionKey);
    }

    @Test
    void shouldReturnFalse_WhenStreamBridgeFailsToSendMessage() {
        // Given
        CapitalMaximizationQueryEvent event = new CapitalMaximizationQueryEvent(2, new BigDecimal("100.00"));
        when(streamBridge.send(eq(CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING), any(Message.class)))
                .thenReturn(false);

        // When
        Mono<Boolean> result = publisher.publishEvent(event);

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(streamBridge, times(1))
                .send(eq(CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING), any(Message.class));
    }

    @Test
    void shouldThrowIllegalArgumentException_WhenEventIsNull() {
        // Because the null-check is done immediately, we assert the exception synchronously
        assertThatThrownBy(() -> publisher.publishEvent(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Capital maximization query event cannot be null");

        verifyNoInteractions(streamBridge);
    }

    @Test
    void shouldGenerateCorrectPartitionKey_WhenPublishingEvent() {
        // Given
        CapitalMaximizationQueryEvent event = new CapitalMaximizationQueryEvent(5, new BigDecimal("5000.00"));
        String expectedPartitionKey = computePartitionKey(event);

        when(streamBridge.send(eq(CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING), any(Message.class)))
                .thenReturn(true);

        // When
        Mono<Boolean> result = publisher.publishEvent(event);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        ArgumentCaptor<Message<CapitalMaximizationQueryEvent>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(streamBridge, times(1))
                .send(eq(CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING), messageCaptor.capture());

        Message<CapitalMaximizationQueryEvent> actualMessage = messageCaptor.getValue();
        assertThat(actualMessage.getHeaders()).containsEntry(PARTITION_KEY_HEADER, expectedPartitionKey);
    }

    @Test
    void shouldPropagateException_WhenStreamBridgeThrowsException() {
        // Given
        CapitalMaximizationQueryEvent event = new CapitalMaximizationQueryEvent(3, new BigDecimal("1000.00"));
        RuntimeException simulatedException = new RuntimeException("Simulated Kafka failure");
        when(streamBridge.send(eq(CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING), any(Message.class)))
                .thenThrow(simulatedException);

        // When
        Mono<Boolean> result = publisher.publishEvent(event);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Simulated Kafka failure"))
                .verify();

        verify(streamBridge, times(1))
                .send(eq(CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING), any(Message.class));
    }

    // Helper method to compute the expected partition key based on the event data
    private String computePartitionKey(CapitalMaximizationQueryEvent event) {
        int hash = Objects.hash(event.maxProjects(), event.initialCapital());
        return Integer.toString(Math.abs(hash) % 10);
    }
}