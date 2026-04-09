package com.anderson.msfraud.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class FraudMetrics {

    private final Counter fraudDetected;
    private final Counter fraudApproved;
    private final Counter blacklistHits;

    public FraudMetrics(MeterRegistry registry) {
        this.fraudDetected = Counter.builder("fraud_detected_total")
                .description("Total de fraudes detectadas")
                .register(registry);

        this.fraudApproved = Counter.builder("fraud_approved_total")
                .description("Total de análises aprovadas")
                .register(registry);

        this.blacklistHits = Counter.builder("fraud_blacklist_hits")
                .description("Total de hits na blacklist")
                .register(registry);
    }

    public void incrementFraudDetected() {
        fraudDetected.increment();
    }

    public void incrementApproved() {
        fraudApproved.increment();
    }

    public void incrementBlacklistHits() {
        blacklistHits.increment();
    }
}
