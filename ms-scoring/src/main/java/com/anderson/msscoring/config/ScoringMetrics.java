package com.anderson.msscoring.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class ScoringMetrics {

    private final Counter scoresCalculated;
    private final Counter cacheHits;
    private final Counter cacheMisses;
    private final Timer scoringDuration;

    public ScoringMetrics(MeterRegistry registry) {
        this.scoresCalculated = Counter.builder("scoring_calculated_total")
                .description("Total de scores calculados")
                .register(registry);

        this.cacheHits = Counter.builder("scoring_cache_hits")
                .description("Total de cache hits")
                .register(registry);

        this.cacheMisses = Counter.builder("scoring_cache_misses")
                .description("Total de cache misses")
                .register(registry);

        this.scoringDuration = Timer.builder("scoring_duration")
                .description("Tempo de cálculo do score")
                .register(registry);
    }

    public void incrementScoresCalculated() {
        scoresCalculated.increment();
    }

    public void incrementCacheHits() {
        cacheHits.increment();
    }

    public void incrementCacheMisses() {
        cacheMisses.increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start();
    }

    public void recordDuration(Timer.Sample sample) {
        sample.stop(scoringDuration);
    }
}
