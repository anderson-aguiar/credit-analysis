package com.anderson.ms_credit.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditRequestEvent(
        String requestId,
        String customerId,
        BigDecimal amount,
        Integer installments,
        String purpose,
        LocalDateTime timestamp
) {}
