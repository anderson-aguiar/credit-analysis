package com.anderson.msscoring.service;

import com.anderson.msscoring.config.ScoringMetrics;
import com.anderson.msscoring.model.CreditHistory;
import com.anderson.msscoring.model.ScoreCategory;
import com.anderson.msscoring.model.ScoreResult;
import com.anderson.msscoring.repository.CreditHistoryRepository;
import com.anderson.msscoring.strategy.AdvancedScoringStrategy;
import com.anderson.msscoring.strategy.BasicScoringStrategy;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    @Mock
    private ScoringMetrics metrics;

    @InjectMocks
    private ScoringService scoringService;

    @BeforeEach
    void setUp() {
        // Necessário para mockar operações do Redis
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Deve retornar score do cache (Redis) e incrementar métrica de Hit")
    void shouldReturnScoreFromCache() {
        String customerId = "cust-123";
        String cacheKey = "score:" + customerId;
        Timer.Sample sample = mock(Timer.Sample.class);

        when(metrics.startTimer()).thenReturn(sample);
        when(valueOperations.get(cacheKey)).thenReturn("750");

        ScoreResult result = scoringService.calculateScore(customerId);

        assertNotNull(result);
        assertEquals(750, result.score());
        assertEquals(ScoreCategory.GOOD, result.category());

        verify(metrics).incrementCacheHits();
        verify(metrics).recordDuration(sample);
        verifyNoInteractions(creditHistoryRepository);
    }

    @Test
    @DisplayName("Deve usar AdvancedStrategy quando cliente tem mais de 12 meses de relacionamento")
    void shouldUseAdvancedStrategyForOldCustomers() {
        String customerId = "cust-old";
        Timer.Sample sample = mock(Timer.Sample.class);
        CreditHistory history = new CreditHistory();
        history.setRelationshipMonths(24);

        when(metrics.startTimer()).thenReturn(sample);
        when(valueOperations.get(anyString())).thenReturn(null); // Cache Miss
        when(creditHistoryRepository.findByCustomerId(customerId)).thenReturn(Optional.of(history));
        when(advancedScoringStrategy.calculateScore(history)).thenReturn(800);

        ScoreResult result = scoringService.calculateScore(customerId);

        assertEquals(800, result.score());
        verify(metrics).incrementCacheMisses();
        verify(advancedScoringStrategy).calculateScore(history);
        verify(valueOperations).set(eq("score:" + customerId), eq("800"), any(Duration.class));
    }

    @Test
    @DisplayName("Deve usar BasicStrategy para novos clientes ou histórico inexistente")
    void shouldUseBasicStrategyForNewCustomers() {
        String customerId = "cust-new";
        Timer.Sample sample = mock(Timer.Sample.class);

        when(metrics.startTimer()).thenReturn(sample);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(creditHistoryRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());
        when(basicScoringStrategy.calculateScore(null)).thenReturn(400);

        ScoreResult result = scoringService.calculateScore(customerId);

        assertEquals(400, result.score());
        verify(basicScoringStrategy).calculateScore(null);
        verify(metrics).incrementScoresCalculated();
    }

    @Test
    @DisplayName("Deve usar BasicStrategy quando meses de relacionamento for nulo ou menor que 12")
    void shouldUseBasicStrategyWhenMonthsIsNull() {
        String customerId = "cust-null-months";
        CreditHistory history = new CreditHistory();
        history.setRelationshipMonths(null);

        when(metrics.startTimer()).thenReturn(mock(Timer.Sample.class));
        when(valueOperations.get(anyString())).thenReturn(null);
        when(creditHistoryRepository.findByCustomerId(customerId)).thenReturn(Optional.of(history));
        when(basicScoringStrategy.calculateScore(history)).thenReturn(300);

        scoringService.calculateScore(customerId);

        verify(basicScoringStrategy).calculateScore(history);
        verifyNoInteractions(advancedScoringStrategy);
    }
}
