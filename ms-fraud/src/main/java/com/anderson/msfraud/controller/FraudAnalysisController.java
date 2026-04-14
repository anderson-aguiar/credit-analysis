package com.anderson.msfraud.controller;

import com.anderson.msfraud.model.FraudAnalysisRequest;
import com.anderson.msfraud.model.FraudAnalysisResponse;
import com.anderson.msfraud.service.FraudAnalysisService;
import com.anderson.msfraud.service.FraudReportService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fraud-analysis")
@CrossOrigin(origins = "*")
public class FraudAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(FraudAnalysisController.class);
    private final FraudAnalysisService analysisService;
    private final FraudReportService fraudReportService;

    public FraudAnalysisController(FraudAnalysisService analysisService, FraudReportService fraudReportService) {
        this.analysisService = analysisService;
        this.fraudReportService = fraudReportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public FraudAnalysisResponse analyzeFraude(@RequestBody @Valid FraudAnalysisRequest request) {
        log.info("Received fraud analysis request: {}", request.requestId());
        return analysisService.analyzeFraude(request);
    }

    @GetMapping(value = "/{requestId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadFraudReport(@PathVariable String requestId) {
        log.info("Generating PDF report for request: {}", requestId);
        byte[] pdfBytes = fraudReportService.generateFraudReportPdf(requestId);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-fraude-" + requestId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
