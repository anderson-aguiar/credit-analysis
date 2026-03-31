package com.anderson.msfraud.validator;

import com.anderson.msfraud.model.FraudAnalysisRequest;
import com.anderson.msfraud.repository.BlacklistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlacklistValidatorTest {

    @Mock
    private BlacklistRepository blacklistRepository;

    @InjectMocks
    private BlacklistValidator validator;

    @Test
    void shouldPassWhenCustomerIsNotBlacklisted() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-1",
                "customer-clean",
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(5000),
                "12345678900"
        );

        when(blacklistRepository.existsByCustomerId("customer-clean")).thenReturn(false);
        when(blacklistRepository.existsByCpf("12345678900")).thenReturn(false);

        // Act
        ValidationResult result = validator.doValidate(request);

        // Assert
        assertTrue(result.valid());
        assertEquals("BlacklistValidator", result.validatorName());
        assertNull(result.reason());

        verify(blacklistRepository, times(1)).existsByCpf("12345678900");
        verify(blacklistRepository, times(1)).existsByCustomerId("customer-clean");
    }



    @Test
    void shouldFailWhenCustomerIdIsBlacklisted() {
        // Arrange
        FraudAnalysisRequest request = new FraudAnalysisRequest(
                "req-3",
                "customer-blocked-001",
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(6000),
                "11122233344"
        );

        when(blacklistRepository.existsByCpf("11122233344")).thenReturn(false);
        when(blacklistRepository.existsByCustomerId("customer-blocked-001")).thenReturn(true);

        // Act
        ValidationResult result = validator.doValidate(request);

        // Assert
        assertFalse(result.valid());
        assertEquals("Cliente na lista negra", result.reason());

        verify(blacklistRepository, times(1)).existsByCpf("11122233344");
        verify(blacklistRepository, times(1)).existsByCustomerId("customer-blocked-001");
    }


}