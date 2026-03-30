package com.anderson.mscredit.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditRequestEvent(
        String requestId,
        String customerId,
        BigDecimal amount,
        Integer installments,
        String purpose,
        String cpf,
        BigDecimal declaredIncome,
        LocalDateTime timestamp
) {}
