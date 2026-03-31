package com.anderson.msscoring.service;

import com.anderson.msscoring.model.CreditHistory;
import com.anderson.msscoring.model.ScoreCategory;
import com.anderson.msscoring.model.ScoreResult;
import com.anderson.msscoring.repository.CreditHistoryRepository;
import com.anderson.msscoring.strategy.AdvancedScoringStrategy;
import com.anderson.msscoring.strategy.BasicScoringStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public
class ScoringServiceTest {

    @Mock
    private CreditHistoryRepository creditHistoryRepository;

    @Mock
    private BasicScoringStrategy basicScoringStrategy;

    @Mock
    private AdvancedScoringStrategy advancedScoringStrategy;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ScoringService scoringService;

    @Test
    void shouldReturnCachedScoreWhenExists() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("score:customer-1")).thenReturn("850");

        // Act
        ScoreResult result = scoringService.calculateScore("customer-1");

        // Assert
        assertNotNull(result);
        assertEquals("customer-1", result.customerId());
        assertEquals(850, result.score());
        assertEquals(ScoreCategory.EXCELLENT, result.category());

        verify(creditHistoryRepository, never()).findByCustomerId(anyString());
        verify(basicScoringStrategy, never()).calculateScore(any());
        verify(advancedScoringStrategy, never()).calculateScore(any());
    }

    @Test
    void shouldUseAdvancedStrategyWhenRelationshipIsGreaterThan12Months() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        CreditHistory history = new CreditHistory(
                "1", "customer-vip", BigDecimal.valueOf(50000),
                60, 0, LocalDateTime.now(), 24
        );

        when(creditHistoryRepository.findByCustomerId("customer-vip")).thenReturn(Optional.of(history));
        when(advancedScoringStrategy.calculateScore(history)).thenReturn(920);

        // Act
        ScoreResult result = scoringService.calculateScore("customer-vip");

        // Assert
        assertEquals(920, result.score());
        assertEquals(ScoreCategory.EXCELLENT, result.category());

        verify(advancedScoringStrategy, times(1)).calculateScore(history);
        verify(basicScoringStrategy, never()).calculateScore(any());
        verify(valueOperations, times(1)).set(eq("score:customer-vip"), eq("920"), eq(Duration.ofHours(24)));
    }

    @Test
    void shouldUseBasicStrategyWhenRelationshipIsLessThan12Months() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        CreditHistory history = new CreditHistory(
                "2", "customer-new", BigDecimal.valueOf(10000),
                24, 2, LocalDateTime.now(), 8
        );

        when(creditHistoryRepository.findByCustomerId("customer-new")).thenReturn(Optional.of(history));
        when(basicScoringStrategy.calculateScore(history)).thenReturn(650);

        // Act
        ScoreResult result = scoringService.calculateScore("customer-new");

        // Assert
        assertEquals(650, result.score());
        assertEquals(ScoreCategory.FAIR, result.category());

        verify(basicScoringStrategy, times(1)).calculateScore(history);
        verify(advancedScoringStrategy, never()).calculateScore(any());
    }

    @Test
    void shouldUseBasicStrategyWhenHistoryIsEmpty() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(creditHistoryRepository.findByCustomerId("customer-first-time")).thenReturn(Optional.empty());
        when(basicScoringStrategy.calculateScore(null)).thenReturn(500);

        // Act
        ScoreResult result = scoringService.calculateScore("customer-first-time");

        // Assert
        assertEquals(500, result.score());
        assertEquals(ScoreCategory.POOR, result.category());

        verify(basicScoringStrategy, times(1)).calculateScore(null);
    }

    @Test
    void shouldCacheScoreAfterCalculation() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(creditHistoryRepository.findByCustomerId(anyString())).thenReturn(Optional.empty());
        when(basicScoringStrategy.calculateScore(null)).thenReturn(550);

        // Act
        scoringService.calculateScore("customer-cache-test");

        // Assert
        verify(valueOperations, times(1)).set(
                eq("score:customer-cache-test"),
                eq("550"),
                eq(Duration.ofHours(24))
        );
    }

}