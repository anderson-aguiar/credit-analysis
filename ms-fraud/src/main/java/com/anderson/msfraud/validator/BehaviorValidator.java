package com.anderson.msfraud.validator;

import com.anderson.msfraud.model.FraudAnalysis;
import com.anderson.msfraud.model.FraudAnalysisRequest;
import com.anderson.msfraud.repository.FraudAnalysisRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BehaviorValidator extends FraudValidator{

    private static final String VALIDATOR_TYPE = "BehaviorValidator";

    private final FraudAnalysisRepository fraudAnalysisRepository;

    public BehaviorValidator(FraudAnalysisRepository fraudAnalysisRepository) {
        this.fraudAnalysisRepository = fraudAnalysisRepository;
    }

    @Override
    protected ValidationResult doValidate(FraudAnalysisRequest request) {
        List<FraudAnalysis> previousAnalyses  =
                fraudAnalysisRepository.findByCustomerId(request.customerId());
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long recentAnalysesCount = previousAnalyses.stream()
                .filter(analysis -> analysis.getAnalyzedAt() != null)
                .filter(analysis -> analysis.getAnalyzedAt().isAfter(last24Hours))
                .count();

        // Se tiver 3 ou mais análises em 24h, comportamento suspeito
        if (recentAnalysesCount >= 3) {
            return ValidationResult.fail(
                    VALIDATOR_TYPE,
                    "Comportamento suspeito: " + recentAnalysesCount + " solicitações nas últimas 24h"
            );
        }

        return ValidationResult.success(VALIDATOR_TYPE);
    }
}
