package com.anderson.msnotification.controller;

import com.anderson.msnotification.model.NotificationLog;
import com.anderson.msnotification.repository.NotificationRepository;
import com.anderson.msnotification.service.SSEService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SSEService sseService;

    @MockitoBean
    private NotificationRepository notificationRepository;

    @Test
    void shouldSubscribeToSSE() throws Exception {
        // Arrange
        SseEmitter mockEmitter = new SseEmitter();
        when(sseService.subscribe("customer-1")).thenReturn(mockEmitter);

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications/stream/customer-1"))
                .andExpect(status().isOk());

        verify(sseService, times(1)).subscribe("customer-1");
    }

    @Test
    void shouldGetNotificationHistory() throws Exception {
        // Arrange
        NotificationLog log1 = new NotificationLog(
                "1", "customer-1", "Crédito aprovado",
                LocalDateTime.now(), true
        );

        NotificationLog log2 = new NotificationLog(
                "2", "customer-1", "Crédito rejeitado",
                LocalDateTime.now().minusDays(1), false
        );

        when(notificationRepository.findByCustomerIdOrderBySentAtDesc("customer-1"))
                .thenReturn(List.of(log1, log2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications/history/customer-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].customerId").value("customer-1"))
                .andExpect(jsonPath("$[0].delivered").value(true));

        verify(notificationRepository, times(1)).findByCustomerIdOrderBySentAtDesc("customer-1");
    }

    @Test
    void shouldReturnEmptyListWhenNoHistory() throws Exception {
        // Arrange
        when(notificationRepository.findByCustomerIdOrderBySentAtDesc("customer-no-history"))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications/history/customer-no-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}