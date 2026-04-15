package com.anderson.msscoring.config;

import com.anderson.msscoring.model.CreditHistory;
import com.anderson.msscoring.repository.CreditHistoryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CreditHistoryRepository repository;

    public DataInitializer(CreditHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing MongoDB with test data...");

        // Limpar dados existentes
        repository.deleteAll();

        // Carregar JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        InputStream inputStream = new ClassPathResource("data/credit-history-init.json").getInputStream();
        List<CreditHistory> histories = mapper.readValue(inputStream, new TypeReference<List<CreditHistory>>() {});

        // Inserir no banco
        repository.saveAll(histories);

        log.info("{} credit histories inserted successfully!", histories.size());
    }
}