package com.anderson.msnotification.controller;

import com.anderson.msnotification.model.NotificationLog;
import com.anderson.msnotification.repository.NotificationRepository;
import com.anderson.msnotification.service.SSEService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final SSEService sseService;
    private final NotificationRepository notificationRepository;


    public NotificationController(SSEService sseService, NotificationRepository notificationRepository) {
        this.sseService = sseService;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/stream/{customerId}")
    public SseEmitter subscribe(@PathVariable String customerId) {
        log.info("Cliente {} conectado ao SSE", customerId);
        return sseService.subscribe(customerId);
    }

    @GetMapping("/history/{customerId}")
    public List<NotificationLog> getHistory(@PathVariable String customerId) {
        log.info("Buscando histórico para o cliente {}", customerId);
        return notificationRepository.findByCustomerIdOrderBySentAtDesc(customerId);
    }
}
