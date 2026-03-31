package com.anderson.msfraud.service;


import com.anderson.msfraud.model.FraudAnalysis;
import com.anderson.msfraud.model.FraudAnalysisRequest;
import com.anderson.msfraud.model.FraudAnalysisResponse;
import com.anderson.msfraud.model.FraudStatus;
import com.anderson.msfraud.repository.FraudAnalysisRepository;
import com.anderson.msfraud.validator.FraudValidator;
import com.anderson.msfraud.validator.ValidationResult;
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

    @InjectMocks
    private FraudAnalysisService fraudAnalysisService;

    @Test
    void shouldApproveWhenValidationPasses() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-123",
                "customer-1",
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(5000),
                "12345678900"
        );

        when(fraudValidationChain.validate(request))
                .thenReturn(ValidationResult.success("AllValidators"));

        // Act
        FraudAnalysisResponse response = fraudAnalysisService.analyzeFraude(request);

        // Assert
        assertNotNull(response);
        assertEquals("req-123", response.requestId());
        assertEquals("customer-1", response.customerId());
        assertEquals(FraudStatus.APPROVED, response.status());
        assertEquals(0, response.riskScore());
        assertNull(response.reason());
        assertTrue(response.failedValidators().isEmpty());

        verify(fraudValidationChain, times(1)).validate(request);
        verify(repository, times(1)).save(any(FraudAnalysis.class));
    }

    @Test
    void shouldRejectWhenValidationFails() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-456",
                "customer-fraud",
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(2000),
                "98765432100"
        );

        when(fraudValidationChain.validate(request))
                .thenReturn(ValidationResult.fail("IncomeValidator", "Valor incompatível com renda"));

        // Act
        FraudAnalysisResponse response = fraudAnalysisService.analyzeFraude(request);

        // Assert
        assertEquals(FraudStatus.REJECTED, response.status());
        assertEquals(100, response.riskScore());
        assertEquals("Valor incompatível com renda", response.reason());
        assertFalse(response.failedValidators().isEmpty());
        assertEquals("IncomeValidator", response.failedValidators().get(0));

        verify(repository, times(1)).save(any(FraudAnalysis.class));
    }

    @Test
    void shouldPersistAnalysisWithCorrectData() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-789",
                "customer-test",
                BigDecimal.valueOf(15000),
                BigDecimal.valueOf(8000),
                "11122233344"
        );

        when(fraudValidationChain.validate(request))
                .thenReturn(ValidationResult.success("AllValidators"));

        ArgumentCaptor<FraudAnalysis> analysisCaptor = ArgumentCaptor.forClass(FraudAnalysis.class);

        // Act
        fraudAnalysisService.analyzeFraude(request);

        // Assert
        verify(repository, times(1)).save(analysisCaptor.capture());

        FraudAnalysis savedAnalysis = analysisCaptor.getValue();
        assertEquals("req-789", savedAnalysis.getRequestId());
        assertEquals("customer-test", savedAnalysis.getCustomerId());
        assertEquals("11122233344", savedAnalysis.getCpf());
        assertEquals(BigDecimal.valueOf(15000), savedAnalysis.getAmount());
        assertEquals(BigDecimal.valueOf(8000), savedAnalysis.getDeclaredIncome());
        assertEquals(FraudStatus.APPROVED, savedAnalysis.getFinalDecision());
        assertNotNull(savedAnalysis.getAnalyzedAt());
    }

    @Test
    void shouldHandleBlacklistValidatorFailure() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-blacklist",
                "customer-blocked",
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(4000),
                "52998224725"
        );

        when(fraudValidationChain.validate(request))
                .thenReturn(ValidationResult.fail("BlacklistValidator", "CPF na lista negra"));

        // Act
        FraudAnalysisResponse response = fraudAnalysisService.analyzeFraude(request);

        // Assert
        assertEquals(FraudStatus.REJECTED, response.status());
        assertEquals("CPF na lista negra", response.reason());
        assertEquals("BlacklistValidator", response.failedValidators().get(0));
    }

    @Test
    void shouldHandleBehaviorValidatorFailure() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-behavior",
                "customer-suspicious",
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(5000),
                "77788899900"
        );

        when(fraudValidationChain.validate(request))
                .thenReturn(ValidationResult.fail("BehaviorValidator", "Comportamento suspeito: 3 solicitações nas últimas 24h"));

        // Act
        FraudAnalysisResponse response = fraudAnalysisService.analyzeFraude(request);

        // Assert
        assertEquals(FraudStatus.REJECTED, response.status());
        assertTrue(response.reason().contains("Comportamento suspeito"));
    }
}