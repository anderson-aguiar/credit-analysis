package com.anderson.msscoring.strategy;

import com.anderson.msscoring.model.CreditHistory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedScoringStrategyTest {

    private final AdvancedScoringStrategy strategy = new AdvancedScoringStrategy();

    @Test
    void shouldReturn600ForNullHistory() {
        // Act
        int score = strategy.calculateScore(null);

        // Assert
        assertEquals(600, score);
    }

    @Test
    void shouldCalculateMaxScoreForPerfectHistory() {
        // Arrange - Cliente VIP: 100% sucesso, 20+ meses, R$200k+
        CreditHistory history = new CreditHistory(
                "1", "customer-vip", BigDecimal.valueOf(250000),
                200, 0, LocalDateTime.now(), 36
        );

        // Act
        int score = strategy.calculateScore(history);

        // Assert - 600 (base) + 400 (100% sucesso) + 100 (20+ meses) + 200 (200k+) = 1000 (limitado)
        assertEquals(1000, score);
    }

    @Test
    void shouldCalculateScoreFor95PercentSuccessRate() {
        // Arrange - 95% de sucesso (190 de 200 pagas em dia)
        CreditHistory history = new CreditHistory(
                "2", "customer-excellent", BigDecimal.valueOf(80000),
                200, 10, LocalDateTime.now(), 24
        );

        // Act
        int score = strategy.calculateScore(history);

        // Assert - 600 + 380 (95% * 400) + 100 (24 meses) + 80 (80k) = 1000 (limitado)
        assertEquals(1000, score);
    }

    @Test
    void shouldCalculateScoreFor80PercentSuccessRate() {
        // Arrange - 80% de sucesso (80 de 100 pagas em dia)
        CreditHistory history = new CreditHistory(
                "3", "customer-good", BigDecimal.valueOf(45000),
                100, 20, LocalDateTime.now(), 18
        );

        // Act
        int score = strategy.calculateScore(history);

        // Assert - 600 + 320 (80% * 400) + 90 (18 meses) + 45 (45k) = 1000 (limitado)
        assertEquals(1000, score);
    }

    @Test
    void shouldCalculateScoreFor50PercentSuccessRate() {
        // Arrange - 50% de sucesso (50 de 100 pagas em dia)
        CreditHistory history = new CreditHistory(
                "4", "customer-avg", BigDecimal.valueOf(15000),
                100, 50, LocalDateTime.now(), 15
        );

        // Act
        int score = strategy.calculateScore(history);

        // Assert - 600 + 200 (50% * 400) + 75 (15 meses) + 15 (15k) = 890
        assertEquals(890, score);
    }

    @Test
    void shouldCalculateMinScoreForPoorHistory() {
        // Arrange - Histórico ruim: muitos atrasos, pouco tempo, baixo volume
        CreditHistory history = new CreditHistory(
                "5", "customer-poor", BigDecimal.valueOf(5000),
                50, 40, LocalDateTime.now(), 8
        );

        // Act
        int score = strategy.calculateScore(history);

        // Assert - 600 + 80 (20% * 400) + 40 (8 meses) + 5 (5k) = 725
        assertEquals(725, score);
    }

    @Test
    void shouldHandleNullFields() {
        // Arrange - Campos nulos
        CreditHistory history = new CreditHistory(
                "6", "customer-null", null, null, null, LocalDateTime.now(), null
        );

        // Act
        int score = strategy.calculateScore(history);

        // Assert - 600 (base) + 0 + 0 + 0 = 600
        assertEquals(600, score);
    }

    @Test
    void shouldNotExceed1000() {
        // Arrange
        CreditHistory history = new CreditHistory(
                "7", "customer-ultra", BigDecimal.valueOf(999999),
                1000, 0, LocalDateTime.now(), 100
        );

        // Act
        int score = strategy.calculateScore(history);

        // Assert
        assertTrue(score <= 1000);
    }

    @Test
    void shouldNotGoBelowZero() {
        // Arrange - Caso extremo
        CreditHistory history = new CreditHistory(
                "8", "customer-zero", BigDecimal.ZERO,
                10, 10, LocalDateTime.now(), 0
        );

        // Act
        int score = strategy.calculateScore(history);

        // Assert
        assertTrue(score >= 0);
    }
}