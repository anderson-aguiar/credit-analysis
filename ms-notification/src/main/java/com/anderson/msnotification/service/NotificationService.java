package com.anderson.msnotification.service;

import com.anderson.msnotification.model.CreditDecisionEvent;
import com.anderson.msnotification.model.NotificationLog;
import com.anderson.msnotification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final SSEService sseService;
    private final NotificationRepository notificationRepository;

    public NotificationService(SSEService sseService, NotificationRepository notificationRepository) {
        this.sseService = sseService;
        this.notificationRepository = notificationRepository;
    }

    public void processNotification(CreditDecisionEvent event) {
        log.info("Processando notificação para o cliente {}", event.customerId());
        String message = customMessage(event);
        boolean sent;
        try {
            sent = sseService.sendNotification(event.customerId(), message);
        } catch (Exception e) {
            log.error("Erro ao enviar SSE para {}: {}", event.customerId(), e.getMessage(), e);
            sent = false;
        }
        LocalDateTime sentAt = event.timestamp() != null ? event.timestamp() : LocalDateTime.now();

        try {
            notificationRepository.save(new NotificationLog(
                    null,
                    event.customerId(),
                    message,
                    sentAt,
                    sent
            ));
        } catch (Exception e) {
            log.error("Erro ao persistir NotificationLog para {}: {}", event.customerId(), e.getMessage(), e);
        }

        log.info("Notificação processada para o cliente {}: delivered={}", event.customerId(), sent);
    }

    private String customMessage(CreditDecisionEvent event) {
        String status = event.status() != null ? event.status() : "";

        String reqId = event.requestId() != null ? event.requestId().substring(0, 8) : "N/A";

        switch (status.toUpperCase()) {
            case "APPROVED" -> {
                BigDecimal approved = event.approvedAmount();
                String value = (approved != null)
                        ? NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(approved)
                        : "valor não informado";

                return "[" + reqId + "] Parabéns! Seu crédito de " + value + " foi aprovado!";
            }
            case "REJECTED" -> {
                String reason = event.reason() != null ? event.reason() : "motivo não informado";
                return "[" + reqId + "] Infelizmente seu crédito foi rejeitado. Motivo: " + reason + ".";
            }
            case "MANUAL_REVIEW" -> {
                return "[" + reqId + "] Sua solicitação está em análise manual. Em breve retornaremos.";
            }
            default -> {
                return "[" + reqId + "] Atualização de crédito: status desconhecido.";
            }
        }
    }

    public void sendPendingNotifications(String customerId) {
        // Busca todas as notificações não entregues deste cliente
        List<NotificationLog> pending = notificationRepository.findByCustomerIdAndDeliveredFalse(customerId);

        for (NotificationLog logEntry : pending) {
            log.info("Reenviando notificação pendente para cliente: {}", customerId);

            // Tenta enviar via SSE
            boolean sent = sseService.sendNotification(customerId, logEntry.getMessage());

            if (sent) {
                // Atualiza o registro existente
                logEntry.setDelivered(true);
                logEntry.setSentAt(LocalDateTime.now());
                notificationRepository.save(logEntry);
            }
        }
    }
}
