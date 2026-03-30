package com.anderson.msfraud.worker;

import com.anderson.msfraud.model.FraudAnalysisRequest;
import com.anderson.msfraud.model.FraudAnalysisResponse;
import com.anderson.msfraud.service.FraudAnalysisService;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class FraudTaskWorker {

    private static final Logger log = LoggerFactory.getLogger(FraudTaskWorker.class);

    private final ExternalTaskClient externalTaskClient;
    private final FraudAnalysisService fraudAnalysisService;

    public FraudTaskWorker(ExternalTaskClient externalTaskClient, FraudAnalysisService fraudAnalysisService) {
        this.externalTaskClient = externalTaskClient;
        this.fraudAnalysisService = fraudAnalysisService;
    }

    @PostConstruct
    public void subscribeToFraudTask() {
        externalTaskClient.subscribe("fraudTask")
                .lockDuration(60000)
                .handler((externalTask, externalTaskService) -> {
                    try {
                        //Ler variáveis do processo
                        String requestId = (String) externalTask.getVariable("requestId");
                        String customerId = (String) externalTask.getVariable("customerId");
                        Double amount = (Double) externalTask.getVariable("amount");
                        String cpf = (String) externalTask.getVariable("cpf");
                        Double declaredIncome = (Double) externalTask.getVariable("declaredIncome");

                        log.info("Processing fraud task for customerId: {} - requestId: {}", customerId, requestId);

                        //Montar request de análise de fraude
                        FraudAnalysisRequest request = new FraudAnalysisRequest(
                                requestId,
                                customerId,
                                BigDecimal.valueOf(amount),
                                BigDecimal.valueOf(declaredIncome),
                                cpf
                        );

                        //Analisar fraude
                        FraudAnalysisResponse response = fraudAnalysisService.analyzeFraude(request);

                        //Preparar variáveis para retornar ao Camunda
                        Map<String, Object> variables = new HashMap<>();
                        boolean isFraud = response.status().name().equals("REJECTED");
                        variables.put("isFraud", isFraud);
                        variables.put("fraudReason", response.reason());

                        log.info("Fraud analysis completed: isFraud={}, reason={} for customerId: {}",
                                isFraud, response.reason(), customerId);

                        //Completar a task
                        externalTaskService.complete(externalTask, variables);

                    } catch (Exception e) {
                        log.error("Error processing fraud task", e);
                        externalTaskService.handleFailure(
                                externalTask,
                                "Error analyzing fraud",
                                e.getMessage(),
                                3,
                                10000
                        );
                    }
                })
                .open();

        log.info("Fraud Task Worker subscribed to 'fraudTask'");
    }
}
