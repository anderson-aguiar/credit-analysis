package com.anderson.mscredit.service;

import com.anderson.mscredit.repository.RateLimitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private RateLimitRepository rateLimitRepository;

    @InjectMocks
    private RateLimitService rateLimitService;

    @Test
    void shouldAllowFirstRequest() {
        // Arrange
        ReflectionTestUtils.setField(rateLimitService, "maxRequests", 3);
        ReflectionTestUtils.setField(rateLimitService, "windowHour", 24L);

        when(rateLimitRepository.getRequestCount("customer-1")).thenReturn(0L);

        // Act
        boolean result = rateLimitService.isAllowed("customer-1");

        // Assert
        assertTrue(result);
        verify(rateLimitRepository, times(1)).incrementRequestCount("customer-1");
        verify(rateLimitRepository, times(1)).setExpiration("customer-1", 24L);
    }

    @Test
    void shouldAllowSecondRequest() {
        // Arrange
        ReflectionTestUtils.setField(rateLimitService, "maxRequests", 3);

        when(rateLimitRepository.getRequestCount("customer-2")).thenReturn(1L);

        // Act
        boolean result = rateLimitService.isAllowed("customer-2");

        // Assert
        assertTrue(result);
        verify(rateLimitRepository, times(1)).incrementRequestCount("customer-2");
        verify(rateLimitRepository, never()).setExpiration(anyString(), anyLong());
    }

    @Test
    void shouldAllowThirdRequest() {
        // Arrange
        ReflectionTestUtils.setField(rateLimitService, "maxRequests", 3);

        when(rateLimitRepository.getRequestCount("customer-3")).thenReturn(2L);

        // Act
        boolean result = rateLimitService.isAllowed("customer-3");

        // Assert
        assertTrue(result);
        verify(rateLimitRepository, times(1)).incrementRequestCount("customer-3");
    }

    @Test
    void shouldBlockFourthRequest() {
        // Arrange
        ReflectionTestUtils.setField(rateLimitService, "maxRequests", 3);

        when(rateLimitRepository.getRequestCount("customer-blocked")).thenReturn(3L);

        // Act
        boolean result = rateLimitService.isAllowed("customer-blocked");

        // Assert
        assertFalse(result);
        verify(rateLimitRepository, never()).incrementRequestCount(anyString());
    }
}