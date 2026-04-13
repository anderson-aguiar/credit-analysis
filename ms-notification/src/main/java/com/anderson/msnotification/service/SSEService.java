package com.anderson.msnotification.service;

import com.anderson.msnotification.config.NotificationMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SSEService {
    private static final Logger log = LoggerFactory.getLogger(SSEService.class);
    private final NotificationMetrics metrics;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final NotificationService notificationService;

    public SSEService(NotificationMetrics metrics, @Lazy NotificationService notificationService) {
        this.metrics = metrics;
        this.notificationService = notificationService;
    }

    public SseEmitter subscribe(String customerId) {
        // Timeout de 5 minutos para evitar conexões infinitas
        SseEmitter emitter = new SseEmitter(300000L);
        emitters.put(customerId, emitter);
        metrics.incrementActiveConnections();

        Runnable cleanup = () -> {
            if (emitters.remove(customerId) != null) {
                metrics.decrementActiveConnections();
                metrics.incrementDisconnected();
                log.info("Conexão SSE encerrada para cliente: {}", customerId);
            }
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError((e) -> cleanup.run());

        try {
            emitter.send(SseEmitter.event().name("ping").data("concluido"));
        } catch (IOException e) {
            cleanup.run();
        }

        CompletableFuture.runAsync(() -> notificationService.sendPendingNotifications(customerId));
        return emitter;
    }

    public boolean sendNotification(String customerId, Object payload) {
        SseEmitter emitter = emitters.get(customerId);

        if (emitter == null) {
            log.warn("Tentativa de envio para cliente offline: {}", customerId);
            metrics.incrementFailed();
            return false;
        }

        try {
            emitter.send(SseEmitter.event().name("credit-decision").data(payload));
            metrics.incrementSent();
            return true;
        } catch (IOException e) {
            log.error("Erro ao enviar SSE para cliente {}: {}", customerId, e.getMessage());
            metrics.incrementFailed();

            return false;
        }
    }
}
