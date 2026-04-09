package com.anderson.mscredit.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CreditMetrics {

    private final Counter creditRequestsTotal;
    private final Counter creditRequestsApproved;
    private final Counter creditRequestsRejected;
    private final Counter rateLimitExceeded;

    public CreditMetrics(MeterRegistry registry) {
        this.creditRequestsTotal = Counter.builder("credit_requests")
                .description("Total de solicitações de crédito recebidas")
                .register(registry);

        this.creditRequestsApproved = Counter.builder("credit_requests_approved")
                .description("Total de solicitações aprovadas")
                .register(registry);

        this.creditRequestsRejected = Counter.builder("credit_requests_rejected")
                .description("Total de solicitações rejeitadas")
                .register(registry);

        this.rateLimitExceeded = Counter.builder("credit_rate_limit_exceeded")
                .description("Total de requisições bloqueadas por rate limit")
                .register(registry);
    }

    public void incrementTotalRequests() {
        creditRequestsTotal.increment();
    }

    public void incrementApproved() {
        creditRequestsApproved.increment();
    }

    public void incrementRejected() {
        creditRequestsRejected.increment();
    }

    public void incrementRateLimitExceeded() {
        rateLimitExceeded.increment();
    }
}
