package com.anderson.msfraud.validator;

import com.anderson.msfraud.model.FraudAnalysisRequest;
import com.anderson.msfraud.repository.BlacklistRepository;
import org.springframework.stereotype.Component;

@Component
public class BlacklistValidator extends FraudValidator {

    private static final String VALIDATOR_TYPE = "BlacklistValidator";
    private final BlacklistRepository blackListRepository;

    public BlacklistValidator(BlacklistRepository blackListRepository) {
        this.blackListRepository = blackListRepository;
    }

    @Override
    protected ValidationResult doValidate(FraudAnalysisRequest request) {
        boolean isBlacklistedByCustomerId =
                blackListRepository.existsByCustomerId(request.customerId());
        boolean isBlacklistedByCpf =
                blackListRepository.existsByCpf(request.cpf());

        if (isBlacklistedByCpf) {
            return ValidationResult.fail(VALIDATOR_TYPE, "CPF na lista negra");
        } else if (isBlacklistedByCustomerId) {
            return ValidationResult.fail(VALIDATOR_TYPE, "Cliente na lista negra");
        }
        return ValidationResult.success(VALIDATOR_TYPE);

    }
}
