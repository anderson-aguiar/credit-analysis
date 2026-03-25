package com.anderson.msfraud.controller;

import com.anderson.msfraud.model.FraudAnalysisRequest;
import com.anderson.msfraud.model.FraudAnalysisResponse;
import com.anderson.msfraud.service.FraudAnalysisService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fraud-analysis")
public class FraudAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(FraudAnalysisController.class);
    private final FraudAnalysisService analysisService;

    public FraudAnalysisController(FraudAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public FraudAnalysisResponse analyzeFraude(@RequestBody @Valid FraudAnalysisRequest request) {
        log.info("Received fraud analysis request: {}", request.requestId());
        return analysisService.analyzeFraude(request);
    }


}
