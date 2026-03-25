package com.anderson.msfraud.validator;

import com.anderson.msfraud.model.FraudAnalysisRequest;

public abstract class FraudValidator {

    protected FraudValidator next;

    public void setNext(FraudValidator next) {
        this.next = next;
    }

    protected abstract ValidationResult doValidate(FraudAnalysisRequest request);

    public ValidationResult validate(FraudAnalysisRequest request) {
        ValidationResult result = doValidate(request);

        // Se falhou, retorna imediatamente (interrompe a cadeia)
        if (!result.valid()) {
            return result;
        }

        // Se passou e existe próximo validador, continua a cadeia
        if (next != null) {
            return next.validate(request);
        }

        // Se passou e não tem próximo, retorna sucesso
        return result;
    }

}
