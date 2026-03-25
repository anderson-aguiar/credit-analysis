package com.anderson.msfraud.validator;

import com.anderson.msfraud.model.FraudAnalysisRequest;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DocumentValidator extends FraudValidator {

    private static final String VALIDATOR_TYPE = "DocumentValidator";
    private static final Pattern CPF_REGEX =
            Pattern.compile("^(?!([0-9])\\1{10})\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$");

    @Override
    protected ValidationResult doValidate(FraudAnalysisRequest request) {

        String cpf = request.cpf();

        if (cpf == null) {
            return ValidationResult.fail(VALIDATOR_TYPE, "CPF não foi fornecido");
        } else if (!CPF_REGEX.matcher(cpf).matches()) {
            return ValidationResult.fail(VALIDATOR_TYPE, "CPF inválido");
        }
        return ValidationResult.success(VALIDATOR_TYPE);
    }


}
