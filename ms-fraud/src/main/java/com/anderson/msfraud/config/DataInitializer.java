package com.anderson.msfraud.config;

import com.anderson.msfraud.model.Blacklist;
import com.anderson.msfraud.repository.BlacklistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final BlacklistRepository repository;

    public DataInitializer(BlacklistRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing PostgreSQL blacklist with test data...");

        // Limpar dados existentes
        repository.deleteAll();

        // Criar lista de registros
        List<Blacklist> blacklists = List.of(
                new Blacklist(null, "customer-blocked-001", "52998224725",
                        "Fraude comprovada em operação anterior", LocalDateTime.of(2024, 6, 15, 10, 30)),
                new Blacklist(null, "customer-blocked-002", "85274196325",
                        "Documentos falsificados", LocalDateTime.of(2024, 8, 22, 14, 45)),
                new Blacklist(null, "customer-blocked-003", "74185296374",
                        "Inadimplência recorrente superior a 180 dias", LocalDateTime.of(2024, 9, 10, 9, 15)),
                new Blacklist(null, "customer-blocked-004", "96385274163",
                        "Envolvimento em esquema de lavagem de dinheiro", LocalDateTime.of(2024, 10, 5, 16, 20)),
                new Blacklist(null, "customer-blocked-005", "15975348625",
                        "Múltiplas identidades detectadas", LocalDateTime.of(2024, 11, 18, 11, 0)),
                new Blacklist(null, "customer-blocked-006", "35795124863",
                        "Histórico de calote em múltiplas instituições", LocalDateTime.of(2024, 12, 2, 8, 30)),
                new Blacklist(null, "customer-blocked-007", "75315948624",
                        "Tentativa de fraude documental", LocalDateTime.of(2025, 1, 10, 13, 45)),
                new Blacklist(null, "customer-blocked-008", "95135748624",
                        "Uso de CPF de terceiros", LocalDateTime.of(2025, 2, 5, 15, 10))
        );

        // Inserir no banco
        repository.saveAll(blacklists);

        log.info("{} blacklist entries inserted successfully!", blacklists.size());
    }
}