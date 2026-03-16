package com.anderson.mscredit.model;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreditRequest(
        @NotBlank(message = "Customer ID is required")
        String customerId,

        @NotNull(message = "Amount cannot be null")
        @Positive(message = "Amount cannot be negative")
        BigDecimal amount,

        @NotNull
        @Min(value = 1, message = "Min 1")
        @Max(value = 72, message = "Max 72")
        Integer installments,

        @NotNull(message = "Credit Purpose cannot be null")
        CreditPurpose purpose
) {
}
