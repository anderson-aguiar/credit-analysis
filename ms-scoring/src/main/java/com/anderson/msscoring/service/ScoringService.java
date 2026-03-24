package com.anderson.msscoring.service;

import com.anderson.msscoring.model.CreditHistory;
import com.anderson.msscoring.model.ScoreCategory;
import com.anderson.msscoring.model.ScoreResult;
import com.anderson.msscoring.repository.CreditHistoryRepository;
import com.anderson.msscoring.strategy.AdvancedScoringStrategy;
import com.anderson.msscoring.strategy.BasicScoringStrategy;
import com.anderson.msscoring.strategy.ScoringStrategy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ScoringService {

    private final CreditHistoryRepository creditHistoryRepository;
    private final BasicScoringStrategy basicScoringStrategy;
    private final AdvancedScoringStrategy advancedScoringStrategy;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String CACHE_PREFIX = "score:";
    private static final long CACHE_TTL_HOURS = 24;

    public ScoringService(CreditHistoryRepository creditHistoryRepository, BasicScoringStrategy basicScoringStrategy,
                          AdvancedScoringStrategy advancedScoringStrategy, RedisTemplate<String, String> redisTemplate) {
        this.creditHistoryRepository = creditHistoryRepository;
        this.basicScoringStrategy = basicScoringStrategy;
        this.advancedScoringStrategy = advancedScoringStrategy;
        this.redisTemplate = redisTemplate;
    }

    public ScoreResult calculateScore(String customerId) {
        String cacheKey = CACHE_PREFIX + customerId;
        String cachedScore = redisTemplate.opsForValue().get(cacheKey);

        if (cachedScore != null) {
            int score = Integer.parseInt(cachedScore);
            ScoreCategory category = ScoreCategory.fromScore(score);
            return new ScoreResult(
                    customerId, score, category, LocalDateTime.now()
            );
        }
        Optional<CreditHistory> historyOptional = creditHistoryRepository.findByCustomerId(customerId);
        ScoringStrategy strategy;
        if (historyOptional.isPresent() && historyOptional.get().getRelationshipMonths() != null
                && historyOptional.get().getRelationshipMonths() >= 12) {
            strategy = advancedScoringStrategy;
        } else {
            strategy = basicScoringStrategy;
        }

        CreditHistory history = historyOptional.orElse(null);
        int score = strategy.calculateScore(history);
        ScoreCategory category = ScoreCategory.fromScore(score);

        //Salva no Redis com expiração 24h
        redisTemplate.opsForValue().set(
                cacheKey,
                String.valueOf(score),
                Duration.ofHours(CACHE_TTL_HOURS)
        );
        return new ScoreResult(
                customerId, score, category, LocalDateTime.now()
        );
    }
}
