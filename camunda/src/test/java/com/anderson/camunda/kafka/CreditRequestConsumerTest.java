package com.anderson.camunda.kafka;

import com.anderson.camunda.model.CreditRequestEvent;
import com.anderson.camunda.service.CamundaProcessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditRequestConsumerTest {

    @Mock
    private CamundaProcessService camundaProcessService;

    @InjectMocks
    private CreditRequestConsumer creditRequestConsumer;

    @Test
    void shouldProcessCreditRequestEvent() {
        CreditRequestEvent event = createEvent("req-123");
        Consumer<CreditRequestEvent> consumer = creditRequestConsumer.processCreditRequest();

        consumer.accept(event);

        verify(camundaProcessService, times(1)).startCreditProcess(event);
    }

    @Test
    void shouldThrowExceptionWhenCamundaFails() {
        // Arrange
        CreditRequestEvent event = createEvent("req-error");

        doThrow(new RuntimeException("Camunda Offline"))
                .when(camundaProcessService).startCreditProcess(any());

        Consumer<CreditRequestEvent> consumer = creditRequestConsumer.processCreditRequest();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            consumer.accept(event);
        });
    }

    @Test
    void shouldVerifyEventData() {
        LocalDateTime now = LocalDateTime.now();
        CreditRequestEvent event = new CreditRequestEvent(
                "req-1", "cust-1", BigDecimal.TEN, 12, "PERSONAL", "123", BigDecimal.ONE, now
        );

        assertEquals("req-1", event.requestId());
        assertEquals("PERSONAL", event.purpose());
        assertNotNull(event.timestamp());
    }

      private CreditRequestEvent createEvent(String id) {
        return new CreditRequestEvent(
                id, "customer-1", BigDecimal.valueOf(10000),
                12, "PERSONAL", "12345678900", BigDecimal.valueOf(5000),
                LocalDateTime.now()
        );
    }
}