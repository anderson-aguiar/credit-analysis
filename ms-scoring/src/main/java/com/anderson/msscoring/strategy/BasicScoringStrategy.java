package com.anderson.msscoring.strategy;

import com.anderson.msscoring.model.CreditHistory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estratégia básica de cálculo de score.
 * Indicada para clientes novos ou com histórico simples.
 * Pontuação base: 500 pontos
 * Máximo possível: 900 pontos (500 base + 200 histórico + 100 relacionamento + 100 volume)
 */
@Component
public class BasicScoringStrategy implements ScoringStrategy {

    /**
     * Calcula o score do cliente baseado em critérios simplificados.
     *
     * @param history Histórico de crédito do cliente (pode ser null para clientes novos)
     * @return Score entre 0 e 1000
     */
    @Override
    public int calculateScore(CreditHistory history) {
        int score = 500;
        if (history == null) {
            return score;
        }
        score += calculateHistoryPoints(history.getLatePayments()) +
                calculateRelationshipPoints(history.getRelationshipMonths()) +
                calculateTotalAmountPoints(history.getTotalAmount());

        return Math.min(Math.max(score, 0), 1000);
    }
    /**
     * Calcula pontos por volume total emprestado (máx 100 pontos - peso 20%).
     * - R$50.000+: 100 pontos
     * - R$10.000-49.999: 50 pontos
     * - Menos de R$10.000: 0 pontos
     */
    private int calculateTotalAmountPoints(BigDecimal totalAmount) {
        if (totalAmount == null) {
            return 0;
        }
        int amount = totalAmount.intValue();
        if (amount >= 50000) {
            return 100;
        } else if (amount >= 10000) {
            return 50;
        }
        return 0;
    }

    /**
     * Calcula pontos por tempo de relacionamento (máx 100 pontos - peso 10%).
     * - 12+ meses: 100 pontos
     * - 6-11 meses: 50 pontos
     * - Menos de 6 meses: 0 pontos
     */
    private int calculateRelationshipPoints(Integer relationshipMonths) {
        if (relationshipMonths == null) {
            return 0;
        }

        if (relationshipMonths >= 12) {
            return 100;
        } else if (relationshipMonths >= 6) {
            return 50;
        }
        return 0;
    }

    /**
     * Calcula pontos baseado no histórico de pagamentos (máx 200 pontos - peso 40%).
     * - 0 atrasos: 200 pontos
     * - 1 ou 2 atrasos: 100 pontos
     * - Mais de 2 atrasos: 0 pontos
     */
    private Integer calculateHistoryPoints(Integer latePayments) {
        if (latePayments == null || latePayments == 0) {
            return 200;
        } else if (latePayments <= 2) {
            return 100;
        }
        return 0;
    }
}
