package com.anderson.msfraud.model;

import java.time.LocalDateTime;
import java.util.List;

public record FraudAnalysisResponse(

        String requestId,
        String customerId,
        FraudStatus status,
        Integer riskScore,
        String reason,
        List<String> failedValidators,
        LocalDateTime analyzedAt
) {}
