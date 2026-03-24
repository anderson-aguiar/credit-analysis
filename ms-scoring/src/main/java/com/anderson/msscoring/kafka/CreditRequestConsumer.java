package com.anderson.msscoring.kafka;

import com.anderson.msscoring.model.CreditRequestEvent;
import com.anderson.msscoring.model.ScoreResult;
import com.anderson.msscoring.service.ScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class CreditRequestConsumer {

    private static final Logger log = LoggerFactory.getLogger(CreditRequestConsumer.class);
    private final ScoringService scoringService;

    public CreditRequestConsumer(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    @Bean
    public Consumer<CreditRequestEvent> processCreditRequest() {
        return event -> {
            log.info("Processing credit request: {}", event.requestId());

            ScoreResult score = scoringService.calculateScore(event.customerId());

            log.info("Score calculated for customer {}: {}", score.customerId(), score.score());
        };
    }
}
