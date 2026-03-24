package com.anderson.msfraud.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.br.CPF;

import java.math.BigDecimal;

public record FraudAnalysisRequest(
        @NotBlank
        String requestId,

        @NotBlank
        String customerId,

        @NotNull
        @Positive(message = "Valor deve ser positivo")
        BigDecimal amount,

        @NotNull
        @Positive(message = "Valor deve ser positivo")
        BigDecimal declaredIncome,

        @NotBlank(message = "CPF deve ser válido")
        @CPF
        String cpf
) {
}
