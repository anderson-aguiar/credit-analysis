package com.anderson.msnotification.kafka;

import com.anderson.msnotification.model.CreditDecisionEvent;
import com.anderson.msnotification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class CreditDecisionConsumer {

    private static final Logger log = LoggerFactory.getLogger(CreditDecisionConsumer.class);
    private final NotificationService notificationService;


    public CreditDecisionConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Bean
    public Consumer<CreditDecisionEvent> processCreditDecision(){
        return event -> {
            log.info("Recebido evento de decisão de crédito para o cliente: {}", event.customerId());

            notificationService.processNotification(event);

            log.info("Evento de decisão de crédito processado para o cliente: {}", event.customerId());
        };
    }
}
