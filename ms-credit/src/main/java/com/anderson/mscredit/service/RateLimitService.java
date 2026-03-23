package com.anderson.mscredit.service;

import com.anderson.mscredit.repository.RateLimitRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {
    private final RateLimitRepository rateLimitRepository;

    @Value("${credit.rate-limit.max-requests}")
    private Integer maxRequests;

    @Value("${credit.rate-limit.window-hours}")
    private Long windowHour;

    public RateLimitService(RateLimitRepository rateLimitRepository) {
        this.rateLimitRepository = rateLimitRepository;

    }

    public boolean isAllowed(String customerId) {
        Long count = rateLimitRepository.getRequestCount(customerId);
        if (count == 0) {
            rateLimitRepository.incrementRequestCount(customerId);
            rateLimitRepository.setExpiration(customerId, windowHour);
            return true;
        } else if (count < maxRequests) {
            rateLimitRepository.incrementRequestCount(customerId);
            return true;
        }
        return false;

    }
}
