package com.github.analytics.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectCapitalOptimizerEventPublisherTest {
    private static final String CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING = "capital-maximization-query-out-0";

    @Mock
    private StreamBridge streamBridge;

    @InjectMocks
    private ProjectCapitalOptimizerEventPublisher underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPublishEvent_successful() {
        // Given
        var event = new CapitalMaximizationQueryEvent(2, new BigDecimal("100.00"));

        // Mock StreamBridge's send method to return true (indicating message was sent)
        when(streamBridge.send(any(), any(Message.class))).thenReturn(true);

        // When
        Mono<Boolean> result = underTest.publishEvent(event);

        // Then
        StepVerifier.create(result)
                .expectNext(true) // Verify that the message was sent (true is returned)
                .verifyComplete();

        ArgumentCaptor<Message<CapitalMaximizationQueryEvent>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(streamBridge, times(1)).send(eq(CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING), messageCaptor.capture());

        Message<CapitalMaximizationQueryEvent> actualMessage = messageCaptor.getValue();
        assertNotNull(actualMessage);

        assertEquals(event, actualMessage.getPayload());
        assertTrue(actualMessage.getHeaders().containsKey("PARTITION_KEY"));
        assertTrue(actualMessage.getHeaders().containsKey("id"));
        assertTrue(actualMessage.getHeaders().containsKey("timestamp"));
    }

    @Test
    void testPublishEvent_failed() {
        // Given
        var event = new CapitalMaximizationQueryEvent(2, new BigDecimal("100.00"));

        // Mock StreamBridge's send method to return false (indicating message failed to send)
        when(streamBridge.send(any(), any(Message.class))).thenReturn(false);

        // When
        Mono<Boolean> result = underTest.publishEvent(event);

        // Then
        StepVerifier.create(result)
                .expectNext(false) // Verify that the message was not sent (false is returned)
                .verifyComplete();

        ArgumentCaptor<Message<CapitalMaximizationQueryEvent>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(streamBridge, times(1)).send(eq(CAPITAL_MAXIMIZATION_QUERY_TOPIC_OUT_BINDING), messageCaptor.capture());

        Message<CapitalMaximizationQueryEvent> actualMessage = messageCaptor.getValue();
        assertNotNull(actualMessage);

        assertEquals(event, actualMessage.getPayload());
        assertTrue(actualMessage.getHeaders().containsKey("PARTITION_KEY"));
        assertTrue(actualMessage.getHeaders().containsKey("id"));
        assertTrue(actualMessage.getHeaders().containsKey("timestamp"));
    }
}