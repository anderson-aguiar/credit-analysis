package com.anderson.camunda.service;

import com.anderson.camunda.model.CreditRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CamundaProcessService {

    private static final Logger log = LoggerFactory.getLogger(CamundaProcessService.class);
    private final RestTemplate restTemplate;

    @Value("${camunda.bpm.client.base-url}")
    private String camundaBaseUrl;

    public CamundaProcessService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void startCreditProcess(CreditRequestEvent event){
        String url = camundaBaseUrl + "/process-definition/key/creditProcess/start";

        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> variables = new HashMap<>();

        variables.put("requestId", createVariable(event.requestId(), "String"));
        variables.put("customerId", createVariable(event.customerId(), "String"));
        variables.put("amount", createVariable(event.amount().doubleValue(), "Double"));
        variables.put("installments", createVariable(event.installments(), "Integer"));
        variables.put("purpose", createVariable(event.purpose(), "String"));
        variables.put("timestamp", createVariable(event.timestamp().toString(), "String"));

        payload.put("variables", variables);
        try {
            log.info("Starting Camunda process for requestId: {}", event.requestId());
            String response = restTemplate.postForObject(url, payload, String.class);
            log.info("Process started successfully: {}", response);
        } catch (Exception e) {
            log.error("Error starting Camunda process for requestId: {}", event.requestId(), e);
        }
    }
    private Map<String, Object> createVariable(Object value, String type) {
        Map<String, Object> variable = new HashMap<>();
        variable.put("value", value);
        variable.put("type", type);
        return variable;
    }
}
