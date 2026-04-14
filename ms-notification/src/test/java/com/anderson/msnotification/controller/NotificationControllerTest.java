package com.anderson.msnotification.controller;

import com.anderson.msnotification.model.NotificationLog;
import com.anderson.msnotification.repository.NotificationRepository;
import com.anderson.msnotification.service.SSEService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    @DisplayName("Deve conectar ao SSE Stream com sucesso")
    void shouldSubscribeToSSE() throws Exception {
        String customerId = "cust-123";
        when(sseService.subscribe(customerId)).thenReturn(new SseEmitter());

        mockMvc.perform(get("/api/v1/notifications/stream/{customerId}", customerId))
                .andExpect(status().isOk());

        verify(sseService).subscribe(customerId);
    }

    @Test
    @DisplayName("Deve buscar histórico paginado com sucesso")
    void shouldGetHistoryPaginado() throws Exception {
        String customerId = "cust-123";
        NotificationLog log = new NotificationLog("1", customerId, "req-1", "Mensagem Teste", LocalDateTime.now(), true);

        // Criando uma página fake para o retorno
        Page<NotificationLog> page = new PageImpl<>(List.of(log));

        when(notificationRepository.findByCustomerId(eq(customerId), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/notifications/history/{customerId}", customerId)
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // No Spring Boot 4 / Jackson, o jsonPath busca no objeto Page retornado
                .andExpect(jsonPath("$.content[0].message").value("Mensagem Teste"))
                .andExpect(jsonPath("$.totalElements").value(1));

        // Verifica se o Sort foi aplicado conforme o Controller (sentAt DESC)
        Pageable expectedPageable = PageRequest.of(0, 5, Sort.by("sentAt").descending());
        verify(notificationRepository).findByCustomerId(customerId, expectedPageable);
    }
}
