package com.anderson.msscoring.strategy;

import com.anderson.msscoring.model.CreditHistory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estratégia avançada de cálculo de score.
 * Indicada para clientes com histórico estabelecido.
 * Utiliza cálculos proporcionais e pesos mais refinados.
 * Pontuação base: 600 pontos
 * Máximo possível: 1000 pontos (600 base + 400 histórico + 100 relacionamento + 200 volume)
 * Diferenças em relação à BasicScoring:
 * - Base mais generosa (600 vs 500)
 * - Taxa de sucesso proporcional ao invés de faixas fixas
 * - Bônus progressivo por tempo de relacionamento
 * - Volume calculado proporcionalmente
 */
@Component
public class AdvancedScoringStrategy implements ScoringStrategy {

    @Override
    public int calculateScore(CreditHistory history) {
        int score = 600;

        if (history == null) {
            return score;
        }

        score += calculatePaymentSuccessRate(history);
        score += calculateRelationshipBonus(history.getRelationshipMonths());
        score += calculateVolumeBonus(history.getTotalAmount());

        return Math.min(Math.max(score, 0), 1000);
    }

    /**
     * Calcula pontos baseado na taxa de sucesso de pagamentos (máx 400 pontos)
     */
    private int calculatePaymentSuccessRate(CreditHistory history) {
        Integer totalInstallments = history.getTotalInstallments();
        Integer latePayments = history.getLatePayments();

        if (totalInstallments == null || totalInstallments == 0) {
            return 0;
        }

        if (latePayments == null) {
            latePayments = 0;
        }

        // Taxa de sucesso: parcelas pagas em dia / total de parcelas
        double successRate = (double) (totalInstallments - latePayments) / totalInstallments;

        // Converte para pontos (máximo 400)
        return (int) (successRate * 400);
    }

    /**
     * Pontos por tempo de relacionamento (máx 100 pontos)
     * 5 pontos por mês, limitado a 100
     */
    private int calculateRelationshipBonus(Integer relationshipMonths) {
        if (relationshipMonths == null) {
            return 0;
        }

        return Math.min(relationshipMonths * 5, 100);
    }

    /**
     * Pontos por volume total emprestado (máx 200 pontos)
     * 1 ponto a cada R$1.000
     */
    private int calculateVolumeBonus(BigDecimal totalAmount) {
        if (totalAmount == null) {
            return 0;
        }

        int volumePoints = (int) (totalAmount.doubleValue() / 1000);

        return Math.min(volumePoints, 200);
    }
}
