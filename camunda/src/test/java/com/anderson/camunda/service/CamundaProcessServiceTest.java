package com.anderson.camunda.service;

import com.anderson.camunda.model.CreditRequestEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CamundaProcessServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CamundaProcessService camundaProcessService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(camundaProcessService, "camundaBaseUrl", "http://localhost:8080/engine-rest");
    }

    @Test
    void shouldStartProcessSuccessfully() {
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

        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("{\"id\":\"process-instance-123\"}");

        // Act
        camundaProcessService.startCreditProcess(event);

        // Assert
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restTemplate, times(1)).postForObject(urlCaptor.capture(), payloadCaptor.capture(), eq(String.class));

        String capturedUrl = urlCaptor.getValue();
        assertTrue(capturedUrl.contains("/process-definition/key/creditProcess/start"));

        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertNotNull(capturedPayload.get("variables"));

        Map<String, Object> variables = (Map<String, Object>) capturedPayload.get("variables");
        assertTrue(variables.containsKey("requestId"));
        assertTrue(variables.containsKey("customerId"));
        assertTrue(variables.containsKey("amount"));
        assertTrue(variables.containsKey("installments"));
        assertTrue(variables.containsKey("purpose"));
        assertTrue(variables.containsKey("cpf"));
        assertTrue(variables.containsKey("declaredIncome"));
    }

    @Test
    void shouldIncludeCorrectVariableTypes() {
        // Arrange
        CreditRequestEvent event = new CreditRequestEvent(
                "req-456",
                "customer-2",
                BigDecimal.valueOf(15000),
                24,
                "HOME_IMPROVEMENT",
                "98765432100",
                BigDecimal.valueOf(8000),
                LocalDateTime.now()
        );

        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("{\"id\":\"process-instance-456\"}");

        // Act
        camundaProcessService.startCreditProcess(event);

        // Assert
        ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(restTemplate, times(1)).postForObject(anyString(), payloadCaptor.capture(), eq(String.class));

        Map<String, Object> payload = payloadCaptor.getValue();
        Map<String, Object> variables = (Map<String, Object>) payload.get("variables");

        Map<String, Object> requestIdVar = (Map<String, Object>) variables.get("requestId");
        assertEquals("String", requestIdVar.get("type"));
        assertEquals("req-456", requestIdVar.get("value"));

        Map<String, Object> amountVar = (Map<String, Object>) variables.get("amount");
        assertEquals("Double", amountVar.get("type"));
        assertEquals(15000.0, amountVar.get("value"));

        Map<String, Object> installmentsVar = (Map<String, Object>) variables.get("installments");
        assertEquals("Integer", installmentsVar.get("type"));
        assertEquals(24, installmentsVar.get("value"));
    }

    @Test
    void shouldHandleRestClientException() {
        // Arrange
        CreditRequestEvent event = new CreditRequestEvent(
                "req-error",
                "customer-error",
                BigDecimal.valueOf(5000),
                12,
                "PERSONAL",
                "11122233344",
                BigDecimal.valueOf(3000),
                LocalDateTime.now()
        );

        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // Act & Assert - não deve lançar exceção (deve apenas logar)
        assertDoesNotThrow(() -> camundaProcessService.startCreditProcess(event));

        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    void shouldConvertBigDecimalToDouble() {
        // Arrange
        CreditRequestEvent event = new CreditRequestEvent(
                "req-789",
                "customer-3",
                new BigDecimal("12345.67"),
                18,
                "VEHICLE",
                "55566677788",
                new BigDecimal("6789.12"),
                LocalDateTime.now()
        );

        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("{\"id\":\"process-instance-789\"}");

        // Act
        camundaProcessService.startCreditProcess(event);

        // Assert
        ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(restTemplate).postForObject(anyString(), payloadCaptor.capture(), eq(String.class));

        Map<String, Object> variables = (Map<String, Object>) payloadCaptor.getValue().get("variables");
        Map<String, Object> amountVar = (Map<String, Object>) variables.get("amount");
        Map<String, Object> incomeVar = (Map<String, Object>) variables.get("declaredIncome");

        assertEquals(12345.67, amountVar.get("value"));
        assertEquals(6789.12, incomeVar.get("value"));
    }

    @Test
    void shouldConvertLocalDateTimeToString() {
        // Arrange
        LocalDateTime now = LocalDateTime.of(2026, 3, 30, 15, 30, 0);
        CreditRequestEvent event = new CreditRequestEvent(
                "req-time",
                "customer-time",
                BigDecimal.valueOf(10000),
                12,
                "EDUCATION",
                "33344455566",
                BigDecimal.valueOf(5000),
                now
        );

        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("{\"id\":\"process-instance-time\"}");

        // Act
        camundaProcessService.startCreditProcess(event);

        // Assert
        ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(restTemplate).postForObject(anyString(), payloadCaptor.capture(), eq(String.class));

        Map<String, Object> variables = (Map<String, Object>) payloadCaptor.getValue().get("variables");
        Map<String, Object> timestampVar = (Map<String, Object>) variables.get("timestamp");

        assertEquals("String", timestampVar.get("type"));
        assertTrue(timestampVar.get("value").toString().contains("2026-03-30"));
    }




    @Test
    void shouldLogErrorWhenCamundaIsDown() {
        // Arrange
        CreditRequestEvent event = new CreditRequestEvent(
                "req-down",
                "customer-down",
                BigDecimal.valueOf(5000),
                12,
                "PERSONAL",
                "99999999999",
                BigDecimal.valueOf(3000),
                LocalDateTime.now()
        );

        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RestClientException("Connection timed out"));

        // Act & Assert
        assertDoesNotThrow(() -> camundaProcessService.startCreditProcess(event));

        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(String.class));
    }
}