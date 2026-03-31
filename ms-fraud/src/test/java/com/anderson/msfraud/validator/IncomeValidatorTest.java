package com.anderson.msfraud.validator;

import com.anderson.msfraud.model.FraudAnalysisRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class IncomeValidatorTest {

    private IncomeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IncomeValidator();
        ReflectionTestUtils.setField(validator, "minIncomeRatio", 0.3);
    }

    @Test
    void shouldPassWhenIncomeIsAdequate() {
        // Arrange - Renda de 10k, pedindo 2k (ratio = 0.2)
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-1",
                "customer-1",
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(10000),
                "12345678900"
        );

        // Act
        ValidationResult result = validator.doValidate(request);

        // Assert
        assertTrue(result.valid());
        assertEquals("IncomeValidator", result.validatorName());
        assertNull(result.reason());
    }

    @Test
    void shouldFailWhenIncomeIsInsufficient() {
        // Arrange - Renda de 2k, pedindo 10k (ratio = 5.0 > 0.3)
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-2",
                "customer-fraud",
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(2000),
                "98765432100"
        );

        // Act
        ValidationResult result = validator.doValidate(request);

        // Assert
        assertFalse(result.valid());
        assertEquals("IncomeValidator", result.validatorName());
        assertEquals("Valor solicitado incompatível com a renda", result.reason());
    }


    @Test
    void shouldPassWhenRatioIsExactlyAtLimit() {
        // Arrange - Renda de 10k, pedindo 3k (ratio = 0.3, exatamente no limite)
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-5",
                "customer-5",
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(10000),
                "33344455566"
        );

        // Act
        ValidationResult result = validator.doValidate(request);

        // Assert
        assertTrue(result.valid());
    }

    @Test
    void shouldFailWhenRatioIsSlightlyAboveLimit() {
        // Arrange - Renda de 10k, pedindo 3.1k (ratio = 0.31 > 0.3)
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-6",
                "customer-6",
                BigDecimal.valueOf(3100),
                BigDecimal.valueOf(10000),
                "66677788899"
        );

        // Act
        ValidationResult result = validator.doValidate(request);

        // Assert
        assertFalse(result.valid());
    }
}