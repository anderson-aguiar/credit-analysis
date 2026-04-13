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
public class ManualReviewWorker {

    private static final Logger log = LoggerFactory.getLogger(ManualReviewWorker.class);
    private final ExternalTaskClient externalTaskClient;
    private final StreamBridge streamBridge;

    public ManualReviewWorker(ExternalTaskClient externalTaskClient, StreamBridge streamBridge) {
        this.externalTaskClient = externalTaskClient;
        this.streamBridge = streamBridge;
    }

    @PostConstruct
    public void notifyManualReview() {
        externalTaskClient.subscribe("notifyManualReviewTask")
                .lockDuration(60000)
                .handler((externalTask, externalTaskService) -> {
                    try {
                        String requestId = (String) externalTask.getVariable("requestId");
                        String customerId = (String) externalTask.getVariable("customerId");

                        log.info("Notifying MANUAL_REVIEW for requestId: {}", requestId);

                        CreditDecisionEvent event = new CreditDecisionEvent(
                                requestId,
                                customerId,
                                "MANUAL_REVIEW",
                                null,
                                BigDecimal.ZERO,
                                LocalDateTime.now()
                        );

                        streamBridge.send("creditDecided-out-0", event);
                        log.info("MANUAL_REVIEW notification sent for requestId: {}", requestId);

                        externalTaskService.complete(externalTask);

                    } catch (Exception e) {
                        log.error("Error notifying manual review", e);
                        externalTaskService.handleFailure(
                                externalTask,
                                "Error notifying manual review",
                                e.getMessage(),
                                3,
                                10000
                        );
                    }
                })
                .open();
    }
}
