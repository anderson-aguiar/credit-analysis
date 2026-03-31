package com.anderson.msnotification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;

class SSEServiceTest {

    private SSEService sseService;

    @BeforeEach
    void setUp() {
        sseService = new SSEService();
    }

    @Test
    void shouldCreateEmitterForNewSubscriber() {
        // Act
        SseEmitter emitter = sseService.subscribe("customer-1");

        // Assert
        assertNotNull(emitter);
    }

    @Test
    void shouldReturnDifferentEmittersForDifferentCustomers() {
        // Act
        SseEmitter emitter1 = sseService.subscribe("customer-1");
        SseEmitter emitter2 = sseService.subscribe("customer-2");

        // Assert
        assertNotEquals(emitter1, emitter2);
    }

    @Test
    void shouldReplaceEmitterWhenCustomerSubscribesAgain() {
        // Act
        SseEmitter emitter1 = sseService.subscribe("customer-1");
        SseEmitter emitter2 = sseService.subscribe("customer-1");

        // Assert
        assertNotEquals(emitter1, emitter2);
    }

    @Test
    void shouldReturnTrueWhenNotificationIsSent() {
        // Arrange
        sseService.subscribe("customer-1");

        // Act
        boolean result = sseService.sendNotification("customer-1", "Test message");

        // Assert
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenCustomerIsNotConnected() {
        // Act
        boolean result = sseService.sendNotification("customer-not-connected", "Test message");

        // Assert
        assertFalse(result);
    }

    @Test
    void shouldSendNotificationToCorrectCustomer() {
        // Arrange
        sseService.subscribe("customer-1");
        sseService.subscribe("customer-2");

        // Act
        boolean result1 = sseService.sendNotification("customer-1", "Message for customer 1");
        boolean result2 = sseService.sendNotification("customer-2", "Message for customer 2");

        // Assert
        assertTrue(result1);
        assertTrue(result2);
    }

    @Test
    void shouldHandleMultipleSubscribers() {
        // Act
        SseEmitter emitter1 = sseService.subscribe("customer-1");
        SseEmitter emitter2 = sseService.subscribe("customer-2");
        SseEmitter emitter3 = sseService.subscribe("customer-3");

        // Assert
        assertNotNull(emitter1);
        assertNotNull(emitter2);
        assertNotNull(emitter3);
    }
}