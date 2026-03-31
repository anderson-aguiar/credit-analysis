package com.anderson.msfraud.validator;

import com.anderson.msfraud.model.FraudAnalysisRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FraudValidatorTest {

    // Validator fake para testar a cadeia
    static class FakePassValidator extends FraudValidator {
        @Override
        protected ValidationResult doValidate(FraudAnalysisRequest request) {
            return ValidationResult.success("FakePass");
        }
    }

    static class FakeFailValidator extends FraudValidator {
        @Override
        protected ValidationResult doValidate(FraudAnalysisRequest request) {
            return ValidationResult.fail("FakeFail", "Fake failure");
        }
    }

    @Test
    void shouldContinueChainWhenValidationPasses() {
        // Arrange
        FakePassValidator validator1 = new FakePassValidator();
        FakePassValidator validator2 = new FakePassValidator();
        validator1.setNext(validator2);

        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-1", "customer-1", BigDecimal.valueOf(10000),
                BigDecimal.valueOf(5000), "12345678900"
        );

        // Act
        ValidationResult result = validator1.validate(request);

        // Assert
        assertTrue(result.valid());
        assertEquals("FakePass", result.validatorName());
    }

    @Test
    void shouldStopChainWhenValidationFails() {
        // Arrange
        FakePassValidator validator1 = new FakePassValidator();
        FakeFailValidator validator2 = new FakeFailValidator();
        FakePassValidator validator3 = new FakePassValidator();

        validator1.setNext(validator2);
        validator2.setNext(validator3);

        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-2", "customer-2", BigDecimal.valueOf(5000),
                BigDecimal.valueOf(3000), "98765432100"
        );

        // Act
        ValidationResult result = validator1.validate(request);

        // Assert
        assertFalse(result.valid());
        assertEquals("FakeFail", result.validatorName());
        assertEquals("Fake failure", result.reason());
    }

    @Test
    void shouldReturnSuccessWhenNoNextValidator() {
        // Arrange
        FakePassValidator validator = new FakePassValidator();

        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-3", "customer-3", BigDecimal.valueOf(8000),
                BigDecimal.valueOf(6000), "11122233344"
        );

        // Act
        ValidationResult result = validator.validate(request);

        // Assert
        assertTrue(result.valid());
    }
}

