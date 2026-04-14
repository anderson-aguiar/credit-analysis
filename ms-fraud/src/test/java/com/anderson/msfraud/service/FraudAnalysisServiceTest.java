package com.anderson.msfraud.service;

import com.anderson.msfraud.config.FraudMetrics;
import com.anderson.msfraud.model.FraudAnalysis;
import com.anderson.msfraud.model.FraudAnalysisRequest;
import com.anderson.msfraud.model.FraudAnalysisResponse;
import com.anderson.msfraud.model.FraudStatus;
import com.anderson.msfraud.repository.FraudAnalysisRepository;
import com.anderson.msfraud.validator.FraudValidator;
import com.anderson.msfraud.validator.ValidationResult;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudAnalysisServiceTest {

    @Mock
    private FraudValidator fraudValidationChain;

    @Mock
    private FraudAnalysisRepository repository;

    @Mock
    private FraudMetrics metrics;

    @InjectMocks
    private FraudAnalysisService fraudAnalysisService;

    @Test
    @DisplayName("Deve aprovar análise e incrementar métrica de aprovação quando o resultado for válido")
    void shouldApproveWhenValidationPasses() {
        // Arrange
        FraudAnalysisRequest request = createRequest("req-123");
        when(fraudValidationChain.validate(request)).thenReturn(ValidationResult.success("AnyValidator"));

        // Act
        FraudAnalysisResponse response = fraudAnalysisService.analyzeFraude(request);

        // Assert
        assertEquals(FraudStatus.APPROVED, response.status());
        verify(metrics).incrementApproved();
        verify(repository).save(any(FraudAnalysis.class));
    }

    @Test
    @DisplayName("Deve rejeitar e incrementar métrica de fraude quando um validador comum falhar")
    void shouldRejectWhenCommonValidatorFails() {
        // Arrange
        FraudAnalysisRequest request = createRequest("req-456");
        when(fraudValidationChain.validate(request))
                .thenReturn(ValidationResult.fail("IncomeValidator", "Renda insuficiente"));

        // Act
        FraudAnalysisResponse response = fraudAnalysisService.analyzeFraude(request);

        // Assert
        assertEquals(FraudStatus.REJECTED, response.status());
        assertEquals("IncomeValidator", response.failedValidators().get(0));

        verify(metrics).incrementFraudDetected();
        // Garante que NÃO chamou a métrica de Blacklist
        verify(metrics, never()).incrementBlacklistHits();
    }

    @Test
    @DisplayName("Deve rejeitar e incrementar métrica de Blacklist especificamente")
    void shouldIncrementBlacklistMetricWhenBlacklistValidatorFails() {
        // Arrange
        FraudAnalysisRequest request = createRequest("req-789");
        // Forçamos o nome do validador para entrar no IF específico do seu Service
        when(fraudValidationChain.validate(request))
                .thenReturn(ValidationResult.fail("BlacklistValidator", "CPF Bloqueado"));

        // Act
        fraudAnalysisService.analyzeFraude(request);

        // Assert
        verify(metrics).incrementFraudDetected();
        verify(metrics).incrementBlacklistHits(); // COBERTURA DA LINHA ESPECÍFICA
    }

    @Test
    @DisplayName("Deve validar se todos os campos da entidade FraudAnalysis foram preenchidos antes de salvar")
    void shouldVerifyEntityMappingBeforeSaving() {
        // Arrange
        FraudAnalysisRequest request = createRequest("req-999");
        when(fraudValidationChain.validate(request)).thenReturn(ValidationResult.success("OK"));
        ArgumentCaptor<FraudAnalysis> captor = ArgumentCaptor.forClass(FraudAnalysis.class);

        // Act
        fraudAnalysisService.analyzeFraude(request);

        // Assert
        verify(repository).save(captor.capture());
        FraudAnalysis saved = captor.getValue();

        assertEquals("req-999", saved.getRequestId());
        assertTrue(saved.getIncomeValid());
        assertTrue(saved.getBlacklistValid());
        assertNotNull(saved.getAnalyzedAt());
    }

    private FraudAnalysisRequest createRequest(String id) {
        return new FraudAnalysisRequest(id, "cust-1", new BigDecimal("1000"), new BigDecimal("5000"), "12345678900");
    }
}
