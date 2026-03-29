package com.anderson.msscoring.worker;

import com.anderson.msscoring.model.ScoreResult;
import com.anderson.msscoring.service.ScoringService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ScoringTaskWorker {

    private static final Logger log = LoggerFactory.getLogger(ScoringTaskWorker.class);

    private final ExternalTaskClient externalTaskClient;
    private final ScoringService scoringService;

    public ScoringTaskWorker(ExternalTaskClient externalTaskClient, ScoringService scoringService) {
        this.externalTaskClient = externalTaskClient;
        this.scoringService = scoringService;
    }

    @PostConstruct
    public void subscribeToScoringTask() {
        externalTaskClient.subscribe("scoringTask")
                .lockDuration(60000)
                .handler((externalTask, externalTaskService) -> {
                    try {
                        //Ler variável customerId
                        String customerId = (String) externalTask.getVariable("customerId");
                        String requestId = (String) externalTask.getVariable("requestId");

                        log.info("Processing scoring task for customerId: {} - requestId: {}", customerId, requestId);

                        //Calcular score
                        ScoreResult scoreResult = scoringService.calculateScore(customerId);

                        //Preparar variáveis para retornar ao Camunda
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("score", scoreResult.score());

                        log.info("Score calculated: {} for customerId: {}", scoreResult.score(), customerId);

                        //Completar a task
                        externalTaskService.complete(externalTask, variables);

                    } catch (Exception e) {
                        log.error("Error processing scoring task", e);
                        externalTaskService.handleFailure(
                                externalTask,
                                "Error calculating score",
                                e.getMessage(),
                                3,
                                10000
                        );
                    }
                })
                .open();

        log.info("Scoring Task Worker subscribed to 'scoringTask'");
    }
}
