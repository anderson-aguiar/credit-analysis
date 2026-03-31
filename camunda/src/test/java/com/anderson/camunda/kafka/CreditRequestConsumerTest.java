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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditRequestConsumerTest {

    @Mock
    private CamundaProcessService camundaProcessService;

    @InjectMocks
    private CreditRequestConsumer creditRequestConsumer;

    @Test
    void shouldProcessCreditRequestEvent() {
        // Arrange
        CreditRequestEvent event = new CreditRequestEvent(
                "req-123",
                "customer-1",
                BigDecimal.valueOf(10000),
                12,
                "PERSONAL",
                "12345678900",
                BigDecimal.valueOf(5000),
                LocalDateTime.now()
        );

        Consumer<CreditRequestEvent> consumer = creditRequestConsumer.processCreditRequest();

        // Act
        consumer.accept(event);

        // Assert
        verify(camundaProcessService, times(1)).startCreditProcess(event);
    }

    @Test
    void shouldHandleMultipleEvents() {
        // Arrange
        CreditRequestEvent event1 = new CreditRequestEvent(
                "req-1", "customer-1", BigDecimal.valueOf(5000),
                12, "PERSONAL", "11111111111", BigDecimal.valueOf(3000), LocalDateTime.now()
        );

        CreditRequestEvent event2 = new CreditRequestEvent(
                "req-2", "customer-2", BigDecimal.valueOf(8000),
                24, "VEHICLE", "22222222222", BigDecimal.valueOf(6000), LocalDateTime.now()
        );

        Consumer<CreditRequestEvent> consumer = creditRequestConsumer.processCreditRequest();

        // Act
        consumer.accept(event1);
        consumer.accept(event2);

        // Assert
        verify(camundaProcessService, times(1)).startCreditProcess(event1);
        verify(camundaProcessService, times(1)).startCreditProcess(event2);
    }
}