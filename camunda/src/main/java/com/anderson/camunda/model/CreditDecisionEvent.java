package com.anderson.camunda.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditDecisionEvent(
        String requestId,
        String customerId,
        String status, //APPROVED, REJECTED, MANUAL_REVIEW
        String reason,
        BigDecimal approvedAmount,
        LocalDateTime timestamp
) {
}