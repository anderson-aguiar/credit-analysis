package com.anderson.msfraud.validator;

import com.anderson.msfraud.model.FraudAnalysisRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DocumentValidatorTest {

    private final DocumentValidator validator = new DocumentValidator();

    @Test
    void shouldAlwaysPassValidation() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-1",
                "customer-1",
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(5000),
                "12345678900"
        );

        // Act
        ValidationResult result = validator.doValidate(request);

        // Assert
        assertTrue(result.valid());
        assertEquals("DocumentValidator", result.validatorName());
        assertNull(result.reason());
    }

    @Test
    void shouldNotPassEvenWithNullCpf() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-2",
                "customer-2",
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(3000),
                null
        );

        // Act
        ValidationResult result = validator.doValidate(request);

        // Assert
        assertFalse(result.valid());
    }
}