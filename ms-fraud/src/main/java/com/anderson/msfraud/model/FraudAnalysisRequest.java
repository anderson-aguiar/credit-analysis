package com.anderson.msfraud.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record FraudAnalysisRequest(
        @JsonProperty("requestId")
        @NotBlank(message = "Request ID is required")
        String requestId,

        @JsonProperty("customerId")
        @NotBlank(message = "Customer ID is required")
        String customerId,

        @JsonProperty("amount")
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @JsonProperty("declaredIncome")
        @NotNull(message = "Declared income is required")
        @Positive(message = "Income must be positive")
        BigDecimal declaredIncome,

        @JsonProperty("cpf")
        @NotBlank(message = "CPF is required")
        String cpf
) {
    @JsonCreator
    public FraudAnalysisRequest {}
}
