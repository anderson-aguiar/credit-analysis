package com.anderson.msnotification.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class NotificationMetrics {

    private final Counter notificationsSent;
    private final Counter notificationsFailed;
    private final Counter notificationsDisconnected;
    private final AtomicInteger activeConnections;
    private final Counter creditApproved;
    private final Counter creditRejected;
    private final Counter creditRequestsProcessed;

    public NotificationMetrics(MeterRegistry registry) {
        this.notificationsSent = Counter.builder("notifications_sent_total")
                .description("Total de notificações enviadas com sucesso")
                .register(registry);

        this.notificationsFailed = Counter.builder("notifications_failed_total")
                .description("Total de notificações que falharam no envio")
                .register(registry);

        this.notificationsDisconnected = Counter.builder("notifications_disconnected_total")
                .description("Total de conexões SSE encerradas (timeout/erro/completion)")
                .register(registry);

        this.activeConnections = new AtomicInteger(0);
        Gauge.builder("notifications_sse_active_connections", activeConnections, AtomicInteger::get)
                .description("Número atual de conexões SSE ativas")
                .register(registry);

        this.creditApproved = Counter.builder("credit_requests_approved")
                .description("Total de créditos aprovados")
                .register(registry);

        this.creditRejected = Counter.builder("credit_requests_rejected")
                .description("Total de créditos rejeitados")
                .register(registry);

        this.creditRequestsProcessed = Counter.builder("credit_requests")
                .description("Total de decisões de crédito processadas")
                .register(registry);

    }

    public void incrementSent() { notificationsSent.increment(); }
    public void incrementFailed() { notificationsFailed.increment(); }
    public void incrementDisconnected() { notificationsDisconnected.increment(); }
    public void incrementActiveConnections() { activeConnections.incrementAndGet(); }
    public void decrementActiveConnections() { activeConnections.decrementAndGet(); }
    public void incrementApproved() { creditApproved.increment(); }
    public void incrementRejected() { creditRejected.increment(); }
    public void incrementProcessed() { creditRequestsProcessed.increment(); }
}
