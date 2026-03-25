package com.anderson.msfraud.config;

import com.anderson.msfraud.validator.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FraudValidationConfig {
    @Bean
    public FraudValidator fraudValidationChain(
            DocumentValidator docValidator,
            IncomeValidator incomeValidator,
            BlacklistValidator blacklistValidator,
            BehaviorValidator behaviorValidator
    ) {
        // Encadeia: doc → income → blacklist → behavior
        docValidator.setNext(incomeValidator);
        incomeValidator.setNext(blacklistValidator);
        blacklistValidator.setNext(behaviorValidator);

        // Retorna o primeiro da cadeia
        return docValidator;
    }
}
