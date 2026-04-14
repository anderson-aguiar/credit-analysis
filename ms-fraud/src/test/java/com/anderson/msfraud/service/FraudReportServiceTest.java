package com.anderson.msfraud.service;

import com.anderson.msfraud.model.FraudAnalysis;
import com.anderson.msfraud.model.FraudStatus;
import com.anderson.msfraud.repository.FraudAnalysisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudReportServiceTest {

    @Mock
    private FraudAnalysisRepository repository;

    @InjectMocks
    private FraudReportService fraudReportService;

    @Test
    @DisplayName("Deve gerar PDF com sucesso para uma análise aprovada")
    void shouldGeneratePdfSuccessfullyForApprovedAnalysis() {
        // Arrange
        String requestId = "req-123";
        FraudAnalysis analysis = createMockAnalysis(requestId, FraudStatus.APPROVED);
        when(repository.findByRequestId(requestId)).thenReturn(Optional.of(analysis));

        // Act
        byte[] pdfBytes = fraudReportService.generateFraudReportPdf(requestId);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        verify(repository, times(1)).findByRequestId(requestId);
    }

    @Test
    @DisplayName("Deve gerar PDF com sucesso para uma análise rejeitada com motivo")
    void shouldGeneratePdfSuccessfullyForRejectedAnalysis() {
        // Arrange
        String requestId = "req-456";
        FraudAnalysis analysis = createMockAnalysis(requestId, FraudStatus.REJECTED);
        analysis.setReason("Renda insuficiente");
        when(repository.findByRequestId(requestId)).thenReturn(Optional.of(analysis));

        // Act
        byte[] pdfBytes = fraudReportService.generateFraudReportPdf(requestId);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        verify(repository, times(1)).findByRequestId(requestId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o Request ID não for encontrado")
    void shouldThrowExceptionWhenRequestIdNotFound() {
        // Arrange
        String requestId = "not-found";
        when(repository.findByRequestId(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fraudReportService.generateFraudReportPdf(requestId);
        });

        assertEquals("Análise não encontrada para o ID: not-found", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException em caso de erro técnico na geração (Cobertura de Catch)")
    void shouldThrowRuntimeExceptionOnTechnicalError() {
        // Arrange
        String requestId = "req-error";
        // Passamos uma análise nula para forçar um erro dentro do try-block ao tentar acessar o status
        when(repository.findByRequestId(requestId)).thenReturn(Optional.of(new FraudAnalysis()));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            fraudReportService.generateFraudReportPdf(requestId);
        });
    }

    // Helper para criar o objeto de análise
    private FraudAnalysis createMockAnalysis(String requestId, FraudStatus status) {
        FraudAnalysis analysis = new FraudAnalysis();
        analysis.setRequestId(requestId);
        analysis.setCustomerId("customer-001");
        analysis.setCpf("123.456.789-00");
        analysis.setAmount(new BigDecimal("10000.00"));
        analysis.setDeclaredIncome(new BigDecimal("5000.00"));
        analysis.setRiskScore(15);
        analysis.setFinalDecision(status);
        analysis.setAnalyzedAt(LocalDateTime.now());
        return analysis;
    }
}
