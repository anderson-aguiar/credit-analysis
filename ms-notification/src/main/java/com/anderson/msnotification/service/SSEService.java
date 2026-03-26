package com.anderson.msnotification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SSEService {

    private static final Logger log = LoggerFactory.getLogger(SSEService.class);

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String customerId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(customerId, emitter);

        emitter.onCompletion(() -> emitters.remove(customerId));
        emitter.onTimeout(() -> emitters.remove(customerId));
        emitter.onError((e) -> emitters.remove(customerId));

        return emitter;
    }

    public boolean sendNotification(String customerId, Object payload) {
        SseEmitter emitter = emitters.get(customerId);

        if (emitter == null) return false;

        try {
            emitter.send(SseEmitter.event().name("credit-decision").data(payload));
            return true;

        } catch (IOException e) {
            log.info("Error ao enviar notificação para o cliente {}", customerId);
            emitters.remove(customerId);
            return false;
        }
    }
}
