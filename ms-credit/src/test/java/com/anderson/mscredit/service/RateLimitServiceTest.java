package com.anderson.mscredit.service;

import com.anderson.mscredit.repository.RateLimitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private RateLimitRepository rateLimitRepository;

    @InjectMocks
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        // Injeta os valores das variáveis @Value
        ReflectionTestUtils.setField(rateLimitService, "maxRequests", 3);
        ReflectionTestUtils.setField(rateLimitService, "windowHour", 24L);
    }

    @Test
    @DisplayName("Deve permitir primeira requisição quando o repositório retornar NULL (Redis vazio)")
    void shouldAllowFirstRequestWhenRepositoryReturnsNull() {
        // Arrange: Simula que a chave não existe no Redis
        when(rateLimitRepository.getRequestCount("cust-null")).thenReturn(null);

        // Act
        boolean result = rateLimitService.isAllowed("cust-null");

        // Assert
        assertTrue(result);
        verify(rateLimitRepository).incrementRequestCount("cust-null");
        verify(rateLimitRepository).setExpiration("cust-null", 24L);
    }

    @Test
    @DisplayName("Deve permitir primeira requisição quando o retorno for ZERO")
    void shouldAllowFirstRequestWhenRepositoryReturnsZero() {
        when(rateLimitRepository.getRequestCount("cust-0")).thenReturn(0L);

        boolean result = rateLimitService.isAllowed("cust-0");

        assertTrue(result);
        verify(rateLimitRepository).incrementRequestCount("cust-0");
        verify(rateLimitRepository).setExpiration("cust-0", 24L);
    }

    @Test
    @DisplayName("Deve permitir segunda requisição sem renovar a expiração")
    void shouldAllowSubsequentRequestUnderLimit() {
        // Arrange: Simula que já existe 1 requisição
        when(rateLimitRepository.getRequestCount("cust-1")).thenReturn(1L);

        // Act
        boolean result = rateLimitService.isAllowed("cust-1");

        // Assert
        assertTrue(result);
        verify(rateLimitRepository).incrementRequestCount("cust-1");
        verify(rateLimitRepository, never()).setExpiration(anyString(), anyLong());
    }

    @Test
    @DisplayName("Deve bloquear quando o limite máximo for atingido")
    void shouldBlockWhenLimitReached() {
        // Arrange: Simula que já atingiu o limite de 3
        when(rateLimitRepository.getRequestCount("cust-max")).thenReturn(3L);

        // Act
        boolean result = rateLimitService.isAllowed("cust-max");

        // Assert
        assertFalse(result);
        verify(rateLimitRepository, never()).incrementRequestCount(anyString());
    }
}
