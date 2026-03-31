package com.anderson.msfraud.validator;

import com.anderson.msfraud.model.FraudAnalysis;
import com.anderson.msfraud.model.FraudAnalysisRequest;
import com.anderson.msfraud.repository.FraudAnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BehaviorValidatorTest {

    @Mock
    private FraudAnalysisRepository fraudAnalysisRepository;

    @InjectMocks
    private BehaviorValidator validator;

    @Test
    void shouldPassWhenNoRecentAnalyses() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-1",
                "customer-new",
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(5000),
                "12345678900"
        );

        when(fraudAnalysisRepository.findByCustomerId("customer-new"))
                .thenReturn(Collections.emptyList());

        // Act
        ValidationResult result = validator.doValidate(request);

        // Assert
        assertTrue(result.valid());
        assertEquals("BehaviorValidator", result.validatorName());
        assertNull(result.reason());

        verify(fraudAnalysisRepository, times(1)).findByCustomerId("customer-new");
    }

    @Test
    void shouldPassWhenOnlyOneRecentAnalysis() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-2",
                "customer-1-request",
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(4000),
                "98765432100"
        );

        FraudAnalysis recentAnalysis = new FraudAnalysis();
        recentAnalysis.setAnalyzedAt(LocalDateTime.now().minusHours(2));

        when(fraudAnalysisRepository.findByCustomerId("customer-1-request"))
                .thenReturn(List.of(recentAnalysis));

        // Act
        ValidationResult result = validator.doValidate(request);

        // Assert
        assertTrue(result.valid());
    }

    @Test
    void shouldPassWhenTwoRecentAnalyses() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-3",
                "customer-2-requests",
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(6000),
                "11122233344"
        );

        FraudAnalysis analysis1 = new FraudAnalysis();
        analysis1.setAnalyzedAt(LocalDateTime.now().minusHours(5));

        FraudAnalysis analysis2 = new FraudAnalysis();
        analysis2.setAnalyzedAt(LocalDateTime.now().minusHours(12));

        when(fraudAnalysisRepository.findByCustomerId("customer-2-requests"))
                .thenReturn(List.of(analysis1, analysis2));

        // Act
        ValidationResult result = validator.doValidate(request);

        // Assert
        assertTrue(result.valid());
    }

    @Test
    void shouldFailWhenThreeRecentAnalyses() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-4",
                "customer-suspicious",
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(5000),
                "55566677788"
        );

        FraudAnalysis analysis1 = new FraudAnalysis();
        analysis1.setAnalyzedAt(LocalDateTime.now().minusHours(1));

        FraudAnalysis analysis2 = new FraudAnalysis();
        analysis2.setAnalyzedAt(LocalDateTime.now().minusHours(8));

        FraudAnalysis analysis3 = new FraudAnalysis();
        analysis3.setAnalyzedAt(LocalDateTime.now().minusHours(20));

        when(fraudAnalysisRepository.findByCustomerId("customer-suspicious"))
                .thenReturn(List.of(analysis1, analysis2, analysis3));

        // Act
        ValidationResult result = validator.doValidate(request);

        // Assert
        assertFalse(result.valid());
        assertEquals("BehaviorValidator", result.validatorName());
        assertTrue(result.reason().contains("Comportamento suspeito"));
        assertTrue(result.reason().contains("3 solicitações"));
    }

    @Test
    void shouldPassWhenAnalysesAreOlderThan24Hours() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-5",
                "customer-old-requests",
                BigDecimal.valueOf(12000),
                BigDecimal.valueOf(7000),
                "33344455566"
        );

        FraudAnalysis oldAnalysis1 = new FraudAnalysis();
        oldAnalysis1.setAnalyzedAt(LocalDateTime.now().minusHours(30));

        FraudAnalysis oldAnalysis2 = new FraudAnalysis();
        oldAnalysis2.setAnalyzedAt(LocalDateTime.now().minusDays(2));

        FraudAnalysis oldAnalysis3 = new FraudAnalysis();
        oldAnalysis3.setAnalyzedAt(LocalDateTime.now().minusDays(5));

        when(fraudAnalysisRepository.findByCustomerId("customer-old-requests"))
                .thenReturn(List.of(oldAnalysis1, oldAnalysis2, oldAnalysis3));

        // Act
        ValidationResult result = validator.doValidate(request);

        // Assert
        assertTrue(result.valid());
    }

    @Test
    void shouldIgnoreAnalysesWithNullTimestamp() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-6",
                "customer-null-timestamp",
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(5000),
                "66677788899"
        );

        FraudAnalysis analysisWithNullDate = new FraudAnalysis();
        analysisWithNullDate.setAnalyzedAt(null);

        FraudAnalysis recentAnalysis = new FraudAnalysis();
        recentAnalysis.setAnalyzedAt(LocalDateTime.now().minusHours(3));

        when(fraudAnalysisRepository.findByCustomerId("customer-null-timestamp"))
                .thenReturn(List.of(analysisWithNullDate, recentAnalysis));

        // Act
        ValidationResult result = validator.doValidate(request);

        // Assert
        assertTrue(result.valid()); // Apenas 1 análise válida (< 3)
    }
}