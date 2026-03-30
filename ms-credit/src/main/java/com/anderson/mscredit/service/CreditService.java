package com.anderson.mscredit.service;

import com.anderson.mscredit.kafka.CreditRequestProducer;
import com.anderson.mscredit.model.CreditRequest;
import com.anderson.mscredit.model.CreditRequestEvent;
import com.anderson.mscredit.model.CreditResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreditService {
    private static final Logger log = LoggerFactory.getLogger(CreditService.class);
    private final RateLimitService rateLimitService;
    private final CreditRequestProducer creditRequestProducer;

    public CreditService(RateLimitService rateLimitService, CreditRequestProducer creditRequestProducer) {
        this.rateLimitService = rateLimitService;
        this.creditRequestProducer = creditRequestProducer;
    }

    public CreditResponse processCreditRequest(CreditRequest request) {
        log.info("Processing credit request for customer: {}", request.customerId());

        if (!rateLimitService.isAllowed(request.customerId())) {
            log.warn("Rate limit exceeded for customer: {}", request.customerId());
            throw new IllegalStateException("Rate limit exceeded. Maximum 3 requests per 24 hours.");
        }

        String requestId = UUID.randomUUID().toString();

        var event = new CreditRequestEvent(
                requestId,
                request.customerId(),
                request.amount(),
                request.installments(),
                request.purpose().name(),
                request.cpf(),
                request.declaredIncome(),
                LocalDateTime.now()
        );

        creditRequestProducer.sendCreditRequested(event);
        log.info("Credit request published successfully: {}", requestId);

        return new CreditResponse(
                requestId,
                request.customerId(),
                request.amount(),
                request.installments(),
                request.purpose(),
                "PENDING",
                LocalDateTime.now()
        );
    }
}
