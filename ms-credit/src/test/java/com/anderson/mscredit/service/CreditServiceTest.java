package com.anderson.mscredit.service;

import com.anderson.mscredit.kafka.CreditRequestProducer;
import com.anderson.mscredit.model.CreditPurpose;
import com.anderson.mscredit.model.CreditRequest;
import com.anderson.mscredit.model.CreditResponse;
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

    @InjectMocks
    private CreditService creditService;

    @Test
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
        assertNotNull(response.requestId());
        assertEquals("customer-123", response.customerId());
        assertEquals(BigDecimal.valueOf(10000), response.amount());
        assertEquals(12, response.installments());
        assertEquals(CreditPurpose.PERSONAL, response.purpose());
        assertEquals("PENDING", response.status());

        verify(rateLimitService, times(1)).isAllowed("customer-123");
        verify(creditRequestProducer, times(1)).sendCreditRequested(any());
    }

    @Test
    void shouldThrowExceptionWhenRateLimitExceeded() {
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
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> creditService.processCreditRequest(request)
        );

        assertEquals("Rate limit exceeded. Maximum 3 requests per 24 hours.", exception.getMessage());
        verify(creditRequestProducer, never()).sendCreditRequested(any());
    }

    @Test
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

        verify(creditRequestProducer, times(1)).sendCreditRequested(eventCaptor.capture());

        com.anderson.mscredit.model.CreditRequestEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent.requestId());
        assertEquals("customer-kafka-test", capturedEvent.customerId());
        assertEquals(BigDecimal.valueOf(15000), capturedEvent.amount());
        assertEquals(24, capturedEvent.installments());
        assertEquals("HOME_IMPROVEMENT", capturedEvent.purpose());
    }

    @Test
    void shouldGenerateUniqueRequestIdForEachRequest() {
        // Arrange
        when(rateLimitService.isAllowed(anyString())).thenReturn(true);

        CreditRequest request1 = new CreditRequest(
                "customer-1", BigDecimal.valueOf(1000), 12,
                CreditPurpose.PERSONAL, "12345678900", BigDecimal.valueOf(3000)
        );

        CreditRequest request2 = new CreditRequest(
                "customer-2", BigDecimal.valueOf(2000), 12,
                CreditPurpose.PERSONAL, "98765432100", BigDecimal.valueOf(4000)
        );

        // Act
        CreditResponse response1 = creditService.processCreditRequest(request1);
        CreditResponse response2 = creditService.processCreditRequest(request2);

        // Assert
        assertNotEquals(response1.requestId(), response2.requestId());
    }
}