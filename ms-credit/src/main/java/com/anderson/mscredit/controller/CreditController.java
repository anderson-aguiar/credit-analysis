package com.anderson.mscredit.controller;

import com.anderson.mscredit.model.CreditRequest;
import com.anderson.mscredit.model.CreditResponse;
import com.anderson.mscredit.service.CreditService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/credit-requests")
public class CreditController {
    private static final Logger log = LoggerFactory.getLogger(CreditController.class);
    private final CreditService creditService;

    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CreditResponse createCreditRequest(@Valid @RequestBody CreditRequest request) {
        log.info("Received credit request for customer: {}", request.customerId());
        return creditService.processCreditRequest(request);
    }
}
