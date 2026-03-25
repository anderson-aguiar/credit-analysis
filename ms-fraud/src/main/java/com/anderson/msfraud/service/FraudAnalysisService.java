package com.anderson.msfraud.service;

import com.anderson.msfraud.model.FraudAnalysis;
import com.anderson.msfraud.model.FraudAnalysisRequest;
import com.anderson.msfraud.model.FraudAnalysisResponse;
import com.anderson.msfraud.model.FraudStatus;
import com.anderson.msfraud.repository.FraudAnalysisRepository;
import com.anderson.msfraud.validator.FraudValidator;
import com.anderson.msfraud.validator.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FraudAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(FraudAnalysisService.class);
    private final FraudValidator fraudValidationChain;
    private final FraudAnalysisRepository repository;

    public FraudAnalysisService(FraudValidator fraudValidationChain, FraudAnalysisRepository repository) {
        this.fraudValidationChain = fraudValidationChain;
        this.repository = repository;
    }

    public FraudAnalysisResponse analyzeFraude(FraudAnalysisRequest request) {
        log.info("Analyzing fraud for request: {}", request.requestId());

        ValidationResult result = fraudValidationChain.validate(request);

        FraudStatus status;
        List<String> failedValidators = new ArrayList<>();
        String reason = null;
        int riskScore = 0;

        if (result.valid()) {
            status = FraudStatus.APPROVED;
        } else {
            status = FraudStatus.REJECTED;
            riskScore = 100;
            failedValidators.add(result.validatorName());
            reason = result.reason();
        }

        FraudAnalysis analysis = new FraudAnalysis();
        analysis.setRequestId(request.requestId());
        analysis.setCustomerId(request.customerId());
        analysis.setCpf(request.cpf());
        analysis.setAmount(request.amount());
        analysis.setDeclaredIncome(request.declaredIncome());

        boolean allValid = result.valid();
        analysis.setDocumentValid(allValid);
        analysis.setIncomeValid(allValid);
        analysis.setBlacklistValid(allValid);
        analysis.setBehaviorValid(allValid);

        analysis.setRiskScore(riskScore);
        analysis.setFinalDecision(status);
        analysis.setReason(reason);
        analysis.setAnalyzedAt(LocalDateTime.now());

        repository.save(analysis);

        return new FraudAnalysisResponse(
                request.requestId(),
                request.customerId(),
                status,
                riskScore,
                reason,
                failedValidators,
                LocalDateTime.now()
        );
    }

}
