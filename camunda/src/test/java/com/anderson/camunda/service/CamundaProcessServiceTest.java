package com.anderson.camunda.service;

import com.anderson.camunda.model.CreditRequestEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    private static final String BASE_URL = "http://localhost:8080/engine-rest";

    @BeforeEach
    void setUp() {
        // Injeta a URL que viria do application.yml
        ReflectionTestUtils.setField(camundaProcessService, "camundaBaseUrl", BASE_URL);
    }

    @Test
    @DisplayName("Deve iniciar o processo no Camunda com sucesso e validar a URL e payload")
    void shouldStartProcessSuccessfully() {
        // Arrange
        CreditRequestEvent event = createEvent("req-123", "customer-1", 10000.0);
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("{\"id\":\"process-instance-123\"}");

        // Act
        camundaProcessService.startCreditProcess(event);

        // Assert
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restTemplate).postForObject(urlCaptor.capture(), payloadCaptor.capture(), eq(String.class));

        // Valida se a URL de Start Process do Camunda está correta
        assertEquals(BASE_URL + "/process-definition/key/creditProcess/start", urlCaptor.getValue());

        // Valida a estrutura das variáveis enviadas ao Camunda
        Map<String, Object> variables = (Map<String, Object>) payloadCaptor.getValue().get("variables");
        assertNotNull(variables);
        assertEquals("req-123", getVarValue(variables, "requestId"));
        assertEquals(10000.0, getVarValue(variables, "amount"));
        assertEquals("customer-1", getVarValue(variables, "customerId"));
    }

    @Test
    @DisplayName("Deve garantir que BigDecimal seja convertido para Double para compatibilidade com Camunda")
    void shouldConvertTypesCorrectly() {
        // Arrange
        CreditRequestEvent event = createEvent("req-789", "cust-3", 12345.67);
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("{\"id\":\"ok\"}");

        // Act
        camundaProcessService.startCreditProcess(event);

        // Assert
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(restTemplate).postForObject(anyString(), payloadCaptor.capture(), any());

        Map<String, Object> variables = (Map<String, Object>) payloadCaptor.getValue().get("variables");

        // Camunda prefere Double para valores numéricos decimais via REST
        assertEquals(12345.67, getVarValue(variables, "amount"));
        assertEquals("Double", getVarType(variables, "amount"));
    }

    @Test
    @DisplayName("Deve converter LocalDateTime para String no payload")
    void shouldConvertLocalDateTimeToString() {
        // Arrange
        LocalDateTime specificTime = LocalDateTime.of(2026, 4, 15, 10, 0);
        CreditRequestEvent event = new CreditRequestEvent(
                "req-time", "cust-time", BigDecimal.TEN, 12, "EDUCATION", "123", BigDecimal.ONE, specificTime
        );

        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn("{\"id\":\"ok\"}");

        // Act
        camundaProcessService.startCreditProcess(event);

        // Assert
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(restTemplate).postForObject(anyString(), payloadCaptor.capture(), any());

        Map<String, Object> variables = (Map<String, Object>) payloadCaptor.getValue().get("variables");
        String timestampVal = (String) getVarValue(variables, "timestamp");

        assertNotNull(timestampVal);
        assertTrue(timestampVal.contains("2026-04-15"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o Camunda estiver fora do ar (Essencial para DLQ)")
    void shouldThrowExceptionWhenCamundaIsDown() {
        // Arrange
        CreditRequestEvent event = createEvent("req-fail", "cust-fail", 100.0);
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RestClientException("Connection Refused"));

        // Act & Assert
        // Verificamos se a exceção sobe. Se subir, o Kafka entende que deve tentar de novo ou mandar pra DLQ.
        assertThrows(RestClientException.class, () -> {
            camundaProcessService.startCreditProcess(event);
        });
    }

    @Test
    @DisplayName("Deve lidar com resposta nula do Camunda sem quebrar")
    void shouldHandleNullResponseGracefully() {
        // Arrange
        CreditRequestEvent event = createEvent("req-null", "cust-null", 100.0);
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn(null);

        // Act & Assert
        assertDoesNotThrow(() -> camundaProcessService.startCreditProcess(event));
        verify(restTemplate, times(1)).postForObject(anyString(), any(), any());
    }

    // --- MÉTODOS AUXILIARES ---

    private CreditRequestEvent createEvent(String reqId, String custId, Double amount) {
        return new CreditRequestEvent(
                reqId, custId, BigDecimal.valueOf(amount),
                12, "PERSONAL", "12345678900", BigDecimal.valueOf(5000),
                LocalDateTime.now()
        );
    }

    private Object getVarValue(Map<String, Object> variables, String key) {
        Map<String, Object> varMap = (Map<String, Object>) variables.get(key);
        return varMap.get("value");
    }

    private String getVarType(Map<String, Object> variables, String key) {
        Map<String, Object> varMap = (Map<String, Object>) variables.get(key);
        return (String) varMap.get("type");
    }
}
