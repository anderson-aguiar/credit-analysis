package com.anderson.camunda.kafka;


import com.anderson.camunda.model.CreditRequestEvent;
import com.anderson.camunda.service.CamundaProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class CreditRequestConsumer {

    private static final Logger log = LoggerFactory.getLogger(CreditRequestConsumer.class);

    private final CamundaProcessService camundaProcessService;

    public CreditRequestConsumer(CamundaProcessService camundaProcessService) {
        this.camundaProcessService = camundaProcessService;
    }

    @Bean
    public Consumer<CreditRequestEvent> processCreditRequest(){
        return event -> {
            log.info("Processing credit request: {}", event.requestId());

            camundaProcessService.startCreditProcess(event);

        };
    }
}
