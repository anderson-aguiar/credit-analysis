package com.anderson.msnotification.service;

import com.anderson.msnotification.model.CreditDecisionEvent;
import com.anderson.msnotification.model.NotificationLog;
import com.anderson.msnotification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private SSEService sseService;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldProcessApprovedNotificationSuccessfully() {
        // Arrange
        CreditDecisionEvent event = new CreditDecisionEvent(
                "req-123",
                "customer-1",
                "APPROVED",
                null,
                BigDecimal.valueOf(10000),
                LocalDateTime.now()
        );

        when(sseService.sendNotification(anyString(), anyString())).thenReturn(true);

        // Act
        notificationService.processNotification(event);

        // Assert
        verify(sseService, times(1)).sendNotification(eq("customer-1"), contains("Parabéns"));
        verify(sseService, times(1)).sendNotification(eq("customer-1"), contains("10"));

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationRepository, times(1)).save(logCaptor.capture());

        NotificationLog savedLog = logCaptor.getValue();
        assertEquals("customer-1", savedLog.getCustomerId());
        assertTrue(savedLog.isDelivered());
        assertTrue(savedLog.getMessage().contains("aprovado"));
    }

    @Test
    void shouldProcessRejectedNotificationSuccessfully() {
        // Arrange
        CreditDecisionEvent event = new CreditDecisionEvent(
                "req-456",
                "customer-fraud",
                "REJECTED",
                "CPF na lista negra",
                BigDecimal.ZERO,
                LocalDateTime.now()
        );

        when(sseService.sendNotification(anyString(), anyString())).thenReturn(true);

        // Act
        notificationService.processNotification(event);

        // Assert
        verify(sseService, times(1)).sendNotification(eq("customer-fraud"), contains("rejeitado"));
        verify(sseService, times(1)).sendNotification(eq("customer-fraud"), contains("CPF na lista negra"));

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationRepository, times(1)).save(logCaptor.capture());

        NotificationLog savedLog = logCaptor.getValue();
        assertEquals("customer-fraud", savedLog.getCustomerId());
        assertTrue(savedLog.isDelivered());
    }

    @Test
    void shouldProcessManualReviewNotification() {
        // Arrange
        CreditDecisionEvent event = new CreditDecisionEvent(
                "req-789",
                "customer-review",
                "MANUAL_REVIEW",
                null,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );

        when(sseService.sendNotification(anyString(), anyString())).thenReturn(true);

        // Act
        notificationService.processNotification(event);

        // Assert
        verify(sseService, times(1)).sendNotification(eq("customer-review"), contains("análise manual"));

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationRepository, times(1)).save(logCaptor.capture());

        NotificationLog savedLog = logCaptor.getValue();
        assertTrue(savedLog.getMessage().contains("análise manual"));
    }

    @Test
    void shouldMarkAsNotDeliveredWhenSSEFails() {
        // Arrange
        CreditDecisionEvent event = new CreditDecisionEvent(
                "req-fail",
                "customer-disconnected",
                "APPROVED",
                null,
                BigDecimal.valueOf(5000),
                LocalDateTime.now()
        );

        when(sseService.sendNotification(anyString(), anyString())).thenReturn(false);

        // Act
        notificationService.processNotification(event);

        // Assert
        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationRepository, times(1)).save(logCaptor.capture());

        NotificationLog savedLog = logCaptor.getValue();
        assertFalse(savedLog.isDelivered());
    }

    @Test
    void shouldHandleSSEException() {
        // Arrange
        CreditDecisionEvent event = new CreditDecisionEvent(
                "req-exception",
                "customer-error",
                "APPROVED",
                null,
                BigDecimal.valueOf(8000),
                LocalDateTime.now()
        );

        when(sseService.sendNotification(anyString(), anyString())).thenThrow(new RuntimeException("SSE error"));

        // Act
        notificationService.processNotification(event);

        // Assert
        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationRepository, times(1)).save(logCaptor.capture());

        NotificationLog savedLog = logCaptor.getValue();
        assertFalse(savedLog.isDelivered());
    }

    @Test
    void shouldHandleUnknownStatus() {
        // Arrange
        CreditDecisionEvent event = new CreditDecisionEvent(
                "req-unknown",
                "customer-unknown",
                "UNKNOWN_STATUS",
                null,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );

        when(sseService.sendNotification(anyString(), anyString())).thenReturn(true);

        // Act
        notificationService.processNotification(event);

        // Assert
        verify(sseService, times(1)).sendNotification(eq("customer-unknown"), contains("status desconhecido"));
    }

    @Test
    void shouldHandleNullApprovedAmount() {
        // Arrange
        CreditDecisionEvent event = new CreditDecisionEvent(
                "req-null-amount",
                "customer-null",
                "APPROVED",
                null,
                null,
                LocalDateTime.now()
        );

        when(sseService.sendNotification(anyString(), anyString())).thenReturn(true);

        // Act
        notificationService.processNotification(event);

        // Assert
        verify(sseService, times(1)).sendNotification(eq("customer-null"), contains("valor não informado"));
    }

    @Test
    void shouldHandleNullReason() {
        // Arrange
        CreditDecisionEvent event = new CreditDecisionEvent(
                "req-null-reason",
                "customer-no-reason",
                "REJECTED",
                null,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );

        when(sseService.sendNotification(anyString(), anyString())).thenReturn(true);

        // Act
        notificationService.processNotification(event);

        // Assert
        verify(sseService, times(1)).sendNotification(eq("customer-no-reason"), contains("motivo não informado"));
    }
}