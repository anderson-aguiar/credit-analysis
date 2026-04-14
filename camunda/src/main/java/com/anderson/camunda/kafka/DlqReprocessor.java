package com.anderson.camunda.kafka;


import com.anderson.camunda.model.CreditRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class DlqReprocessor {

    private static final String PRODUCER_TOPIC = "reprocess-out-0";
    private static final Logger log = LoggerFactory.getLogger(DlqReprocessor.class);
    private final StreamBridge streamBridge;

    public DlqReprocessor(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Bean
    public Consumer<CreditRequestEvent> reprocessDlq() {
        return event -> {

            log.info("Reprocessando mensagem da DLQ: requestId={}", event.requestId());

            boolean sent = streamBridge.send(PRODUCER_TOPIC, event);

            if (sent) {
                log.info("Mensagem devolvida com sucesso para a fila principal: {}", event.requestId());
            } else {
                log.error("Falha ao tentar devolver mensagem para a fila principal.");
            }
        };
    }
}