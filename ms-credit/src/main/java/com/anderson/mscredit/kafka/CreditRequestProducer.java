package com.anderson.mscredit.kafka;

import com.anderson.mscredit.model.CreditRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class CreditRequestProducer {
    private static final Logger log = LoggerFactory.getLogger(CreditRequestProducer.class);
    private final StreamBridge streamBridge;
    private static final String PRODUCER_TOPIC = "creditRequested-out-0";

    public CreditRequestProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendCreditRequested(CreditRequestEvent event) {
        log.info("Publishing credit request event: {}", event.requestId());
        streamBridge.send(PRODUCER_TOPIC, event);
    }
}
