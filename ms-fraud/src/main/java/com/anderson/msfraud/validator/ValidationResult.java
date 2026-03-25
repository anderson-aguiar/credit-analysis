package com.anderson.msfraud.validator;

public record ValidationResult(

        boolean valid,
        String validatorName,
        String reason

) {
    public static ValidationResult success(String validatorName) {
        return new ValidationResult(true, validatorName, null);
    }

    public static ValidationResult fail(String validatorName, String reason) {
        return new ValidationResult(false, validatorName, reason);
    }
}
