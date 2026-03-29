package com.anderson.camunda.worker;

import com.anderson.camunda.model.CreditDecisionEvent;
import jakarta.annotation.PostConstruct;
import org.camunda.bpm.client.ExternalTaskClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class PublishDecisionWorker {
    private static final Logger log = LoggerFactory.getLogger(PublishDecisionWorker.class);
    private final ExternalTaskClient externalTaskClient;
    private final StreamBridge streamBridge;

    public PublishDecisionWorker(ExternalTaskClient externalTaskClient, StreamBridge streamBridge) {
        this.externalTaskClient = externalTaskClient;
        this.streamBridge = streamBridge;
    }
    @PostConstruct
    public void publishDecision() {
        externalTaskClient.subscribe("publishDecisionTask")
                .lockDuration(60000)
                .handler((externalTask, externalTaskService) -> {
                    try {
                        // Ler variáveis do processo
                        String requestId = (String) externalTask.getVariable("requestId");
                        String customerId = (String) externalTask.getVariable("customerId");
                        Double amount = (Double) externalTask.getVariable("amount");
                        Integer score = (Integer) externalTask.getVariable("score");
                        Boolean isFraud = (Boolean) externalTask.getVariable("isFraud");
                        String fraudReason = (String) externalTask.getVariable("fraudReason");

                        // Calcular decisão
                        String decision;
                        if (isFraud != null && isFraud) {
                            decision = "REJECTED";
                        } else if (score != null && score >= 700) {
                            decision = "APPROVED";
                        } else {
                            decision = "MANUAL_REVIEW";
                        }

                        // Montar evento
                        CreditDecisionEvent event = new CreditDecisionEvent(
                                requestId,
                                customerId,
                                decision,
                                isFraud != null && isFraud ? fraudReason : null,
                                decision.equals("APPROVED") ? BigDecimal.valueOf(amount) : BigDecimal.ZERO,
                                LocalDateTime.now()
                        );

                        // Publicar no Kafka
                        streamBridge.send("creditDecided-out-0", event);

                        log.info("Published decision for requestId: {} - decision: {}", requestId, decision);

                        // Completar task
                        externalTaskService.complete(externalTask);

                    } catch (Exception e) {
                        log.error("Error processing publishDecisionTask", e);
                        externalTaskService.handleFailure(externalTask,
                                "Error processing decision",
                                e.getMessage(),
                                3,
                                10000);
                    }
                })
                .open();
    }
}
