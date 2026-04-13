package com.anderson.msnotification.service;

import com.anderson.msnotification.config.NotificationMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class SSEServiceTest {

    private SSEService sseService;
    private NotificationMetrics metrics;
    private NotificationService notificationService;// Mock das métricas

    @BeforeEach
    void setUp() {
        // Criamos um mock para não precisar configurar o MeterRegistry real
        metrics = Mockito.mock(NotificationMetrics.class);
        sseService = new SSEService(metrics, notificationService);
    }

    @Test
    void shouldCreateEmitterForNewSubscriber() {
        // Act
        SseEmitter emitter = sseService.subscribe("customer-1");

        // Assert
        assertNotNull(emitter);
        // Verifica se o serviço avisou as métricas que uma conexão abriu
        verify(metrics).incrementActiveConnections();
    }

    @Test
    void shouldReturnDifferentEmittersForDifferentCustomers() {
        SseEmitter emitter1 = sseService.subscribe("customer-1");
        SseEmitter emitter2 = sseService.subscribe("customer-2");

        assertNotEquals(emitter1, emitter2);
    }

    @Test
    void shouldReplaceEmitterWhenCustomerSubscribesAgain() {
        SseEmitter emitter1 = sseService.subscribe("customer-1");
        SseEmitter emitter2 = sseService.subscribe("customer-1");

        assertNotEquals(emitter1, emitter2);
        // Deve ter incrementado duas vezes (uma para cada subscribe)
        verify(metrics, Mockito.times(2)).incrementActiveConnections();
    }

    @Test
    void shouldReturnTrueWhenNotificationIsSent() {
        sseService.subscribe("customer-1");

        // Act
        boolean result = sseService.sendNotification("customer-1", "Test message");

        // Assert
        assertTrue(result);
        verify(metrics).incrementSent();
    }

    @Test
    void shouldReturnFalseWhenCustomerIsNotConnected() {
        // Act
        boolean result = sseService.sendNotification("customer-not-connected", "Test message");

        // Assert
        assertFalse(result);
        verify(metrics).incrementFailed();
    }

    @Test
    void shouldSendNotificationToCorrectCustomer() {
        sseService.subscribe("customer-1");
        sseService.subscribe("customer-2");

        boolean result1 = sseService.sendNotification("customer-1", "Message 1");
        boolean result2 = sseService.sendNotification("customer-2", "Message 2");

        assertTrue(result1);
        assertTrue(result2);
        verify(metrics, Mockito.times(2)).incrementSent();
    }

    @Test
    void shouldHandleMultipleSubscribers() {
        assertNotNull(sseService.subscribe("customer-1"));
        assertNotNull(sseService.subscribe("customer-2"));
        assertNotNull(sseService.subscribe("customer-3"));

        verify(metrics, Mockito.times(3)).incrementActiveConnections();
    }
}
