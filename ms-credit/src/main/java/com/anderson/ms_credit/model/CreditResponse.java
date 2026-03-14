package com.anderson.ms_credit.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditResponse(
        String requestId,
        String customerId,
        BigDecimal amount,
        Integer installments,
        CreditPurpose purpose,
        String status,
        LocalDateTime createdAt
) {
}
