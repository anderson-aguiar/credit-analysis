package com.anderson.msnotification.service;

import com.anderson.msnotification.model.CreditDecisionEvent;
import com.anderson.msnotification.model.NotificationLog;
import com.anderson.msnotification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    @DisplayName("Deve processar notificação APPROVED e salvar no banco com sucesso")
    void shouldProcessApprovedNotification() {
        String requestId = UUID.randomUUID().toString();
        CreditDecisionEvent event = new CreditDecisionEvent(requestId, "cust-1", "APPROVED", null, new BigDecimal("1000"), LocalDateTime.now());

        when(sseService.sendNotification(eq("cust-1"), anyString())).thenReturn(true);

        notificationService.processNotification(event);

        verify(sseService).sendNotification(eq("cust-1"), contains("Parabéns"));
        verify(notificationRepository).save(argThat(log -> log.isDelivered() && log.getRequestId().equals(requestId)));
    }

    @Test
    @DisplayName("Deve processar notificação REJECTED com motivo")
    void shouldProcessRejectedNotification() {
        String requestId = UUID.randomUUID().toString();
        CreditDecisionEvent event = new CreditDecisionEvent(requestId, "cust-1", "REJECTED", "Renda insuficiente", BigDecimal.ZERO, LocalDateTime.now());

        notificationService.processNotification(event);

        verify(sseService).sendNotification(eq("cust-1"), contains("rejeitado"));
        verify(sseService).sendNotification(eq("cust-1"), contains("Renda insuficiente"));
    }

    @Test
    @DisplayName("Deve processar MANUAL_REVIEW corretamente")
    void shouldProcessManualReview() {
        String requestId = UUID.randomUUID().toString();
        CreditDecisionEvent event = new CreditDecisionEvent(requestId, "cust-1", "MANUAL_REVIEW", null, BigDecimal.ZERO, LocalDateTime.now());

        notificationService.processNotification(event);

        verify(sseService).sendNotification(eq("cust-1"), contains("análise manual"));
    }

    @Test
    @DisplayName("Deve processar status desconhecido usando o bloco default")
    void shouldProcessUnknownStatus() {
        String requestId = UUID.randomUUID().toString();
        CreditDecisionEvent event = new CreditDecisionEvent(requestId, "cust-1", "UNKNOWN", null, BigDecimal.ZERO, LocalDateTime.now());

        notificationService.processNotification(event);

        verify(sseService).sendNotification(eq("cust-1"), contains("status desconhecido"));
    }

    @Test
    @DisplayName("Deve tratar erro no envio SSE e marcar como não entregue no banco")
    void shouldHandleSSEError() {
        String requestId = UUID.randomUUID().toString();
        CreditDecisionEvent event = new CreditDecisionEvent(requestId, "cust-1", "APPROVED", null, BigDecimal.TEN, LocalDateTime.now());

        // Simula uma exceção no serviço de SSE
        when(sseService.sendNotification(any(), any())).thenThrow(new RuntimeException("SSE Offline"));

        notificationService.processNotification(event);

        verify(notificationRepository).save(argThat(log -> !log.isDelivered()));
    }

    @Test
    @DisplayName("Deve capturar erro de banco de dados sem interromper o fluxo (Bloco Catch do Repositório)")
    void shouldHandleRepositoryError() {
        String requestId = UUID.randomUUID().toString();
        CreditDecisionEvent event = new CreditDecisionEvent(requestId, "cust-1", "APPROVED", null, BigDecimal.TEN, LocalDateTime.now());

        when(notificationRepository.save(any())).thenThrow(new RuntimeException("MongoDB Error"));

        assertDoesNotThrow(() -> notificationService.processNotification(event));
    }

    @Test
    @DisplayName("Deve reenviar todas as notificações pendentes de um cliente")
    void shouldSendPendingNotifications() {
        String customerId = "cust-pending";
        NotificationLog pendingLog1 = new NotificationLog("id-1", customerId, "req-1", "Msg 1", LocalDateTime.now(), false);
        NotificationLog pendingLog2 = new NotificationLog("id-2", customerId, "req-2", "Msg 2", LocalDateTime.now(), false);

        when(notificationRepository.findByCustomerIdAndDeliveredFalse(customerId)).thenReturn(List.of(pendingLog1, pendingLog2));
        when(sseService.sendNotification(eq(customerId), anyString())).thenReturn(true);

        notificationService.sendPendingNotifications(customerId);

        // Verifica se tentou enviar as duas
        verify(sseService, times(2)).sendNotification(eq(customerId), anyString());
        // Verifica se salvou as duas como entregues
        verify(notificationRepository, times(2)).save(argThat(NotificationLog::isDelivered));
    }
}
