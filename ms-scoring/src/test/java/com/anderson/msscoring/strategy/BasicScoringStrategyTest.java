package com.anderson.msscoring.strategy;

import com.anderson.msscoring.model.CreditHistory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicScoringStrategyTest {

    private final BasicScoringStrategy strategy = new BasicScoringStrategy();

    @Test
    void shouldReturn500ForNullHistory() {
        // Act
        int score = strategy.calculateScore(null);

        // Assert
        assertEquals(500, score);
    }

    @Test
    void shouldCalculateMaxScoreForPerfectHistory() {
        // Arrange - Cliente perfeito: sem atrasos, 12+ meses, R$50k+
        CreditHistory history = new CreditHistory(
                "1", "customer-perfect", BigDecimal.valueOf(60000),
                100, 0, LocalDateTime.now(), 24
        );

        // Act
        int score = strategy.calculateScore(history);

        // Assert - 500 (base) + 200 (sem atrasos) + 100 (12+ meses) + 100 (50k+) = 900
        assertEquals(900, score);
    }

    @Test
    void shouldCalculateScoreForGoodHistory() {
        // Arrange - Cliente bom: 1 atraso, 12+ meses, R$30k
        CreditHistory history = new CreditHistory(
                "2", "customer-good", BigDecimal.valueOf(30000),
                60, 1, LocalDateTime.now(), 18
        );

        // Act
        int score = strategy.calculateScore(history);

        // Assert - 500 + 100 (1-2 atrasos) + 100 (12+ meses) + 50 (10k-49k) = 750
        assertEquals(750, score);
    }

    @Test
    void shouldCalculateScoreForAverageHistory() {
        // Arrange - Cliente médio: 2 atrasos, 8 meses, R$8k
        CreditHistory history = new CreditHistory(
                "3", "customer-avg", BigDecimal.valueOf(8000),
                24, 2, LocalDateTime.now(), 8
        );

        // Act
        int score = strategy.calculateScore(history);

        // Assert - 500 + 100 (1-2 atrasos) + 50 (6-11 meses) + 0 (<10k) = 650
        assertEquals(650, score);
    }

    @Test
    void shouldCalculateMinScoreForPoorHistory() {
        // Arrange - Cliente ruim: muitos atrasos, novo, baixo volume
        CreditHistory history = new CreditHistory(
                "4", "customer-poor", BigDecimal.valueOf(2000),
                12, 10, LocalDateTime.now(), 3
        );

        // Act
        int score = strategy.calculateScore(history);

        // Assert - 500 + 0 (muitos atrasos) + 0 (<6 meses) + 0 (<10k) = 500
        assertEquals(500, score);
    }

    @Test
    void shouldNotExceed1000() {
        // Arrange - Valores absurdos para testar o limite superior
        CreditHistory history = new CreditHistory(
                "5", "customer-ultra", BigDecimal.valueOf(999999),
                1000, 0, LocalDateTime.now(), 100
        );

        // Act
        int score = strategy.calculateScore(history);

        // Assert
        assertTrue(score <= 1000);
    }

    @Test
    void shouldNotGoBelowZero() {
        // Arrange - Histórico vazio mas válido
        CreditHistory history = new CreditHistory(
                "6", "customer-zero", BigDecimal.ZERO,
                0, 50, LocalDateTime.now(), 0
        );

        // Act
        int score = strategy.calculateScore(history);

        // Assert
        assertTrue(score >= 0);
    }
}