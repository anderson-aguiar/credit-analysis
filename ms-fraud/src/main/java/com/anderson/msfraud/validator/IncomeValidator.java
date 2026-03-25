package com.anderson.msfraud.validator;

import com.anderson.msfraud.model.FraudAnalysisRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class IncomeValidator extends FraudValidator {

    private static final String VALIDATOR_TYPE = "IncomeValidator";
    @Value("${fraud.validation.min-income-ratio}")
    private Double minIncomeRatio;

    @Override
    protected ValidationResult doValidate(FraudAnalysisRequest request) {

        BigDecimal declaredIncome = request.declaredIncome();

        BigDecimal amount = request.amount();

        double ratio = amount.divide(declaredIncome, 2, RoundingMode.HALF_UP).doubleValue();

        if (declaredIncome == null || declaredIncome.equals(BigDecimal.ZERO)) {
            return ValidationResult.fail(VALIDATOR_TYPE, "Renda não declarada");
        } else if (ratio > minIncomeRatio) {
            return ValidationResult.fail(VALIDATOR_TYPE,
                    "Valor solicitado incompatível com a renda");
        }
        return ValidationResult.success(VALIDATOR_TYPE);
    }
}
