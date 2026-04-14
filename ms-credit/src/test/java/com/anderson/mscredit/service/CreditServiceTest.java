package com.anderson.mscredit.service;

import com.anderson.mscredit.config.CreditMetrics;
import com.anderson.mscredit.kafka.CreditRequestProducer;
import com.anderson.mscredit.model.CreditPurpose;
import com.anderson.mscredit.model.CreditRequest;
import com.anderson.mscredit.model.CreditResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditServiceTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private CreditRequestProducer creditRequestProducer;

    @Mock
    private CreditMetrics metrics;

    @InjectMocks
    private CreditService creditService;

    @Test
    @DisplayName("Deve processar solicitação com sucesso e incrementar métricas")
    void shouldProcessCreditRequestSuccessfully() {
        // Arrange
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);

        CreditRequest request = new CreditRequest(
                "customer-123",
                BigDecimal.valueOf(10000),
                12,
                CreditPurpose.PERSONAL,
                "12345678900",
                BigDecimal.valueOf(5000)
        );

        // Act
        CreditResponse response = creditService.processCreditRequest(request);

        // Assert
        assertNotNull(response);
        assertEquals("PENDING", response.status());

        // Verificações de negócio
        verify(rateLimitService, times(1)).isAllowed("customer-123");
        verify(creditRequestProducer, times(1)).sendCreditRequested(any());

        verify(metrics, times(1)).incrementTotalRequests();
    }

    @Test
    @DisplayName("Deve lançar exceção e incrementar métrica de erro quando Rate Limit exceder")
    void shouldThrowExceptionAndTrackMetricWhenRateLimitExceeded() {
        // Arrange
        when(rateLimitService.isAllowed(anyString())).thenReturn(false);

        CreditRequest request = new CreditRequest(
                "customer-blocked",
                BigDecimal.valueOf(5000),
                10,
                CreditPurpose.VEHICLE,
                "98765432100",
                BigDecimal.valueOf(8000)
        );

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> creditService.processCreditRequest(request));

        verify(metrics, times(1)).incrementRateLimitExceeded();
        verify(creditRequestProducer, never()).sendCreditRequested(any());
    }

    @Test
    @DisplayName("Deve mapear corretamente os dados do Request para o Evento do Kafka")
    void shouldPublishEventToKafkaWithCorrectData() {
        // Arrange
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);

        CreditRequest request = new CreditRequest(
                "customer-kafka-test",
                BigDecimal.valueOf(15000),
                24,
                CreditPurpose.HOME_IMPROVEMENT,
                "11122233344",
                BigDecimal.valueOf(7000)
        );

        // Act
        creditService.processCreditRequest(request);

        // Assert
        ArgumentCaptor<com.anderson.mscredit.model.CreditRequestEvent> eventCaptor =
                ArgumentCaptor.forClass(com.anderson.mscredit.model.CreditRequestEvent.class);

        verify(creditRequestProducer).sendCreditRequested(eventCaptor.capture());

        com.anderson.mscredit.model.CreditRequestEvent capturedEvent = eventCaptor.getValue();
        assertEquals("customer-kafka-test", capturedEvent.customerId());
        assertEquals("HOME_IMPROVEMENT", capturedEvent.purpose());
        assertNotNull(capturedEvent.timestamp());
    }
}
