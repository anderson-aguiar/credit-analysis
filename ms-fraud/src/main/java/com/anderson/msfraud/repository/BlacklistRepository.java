package com.anderson.msfraud.repository;

import com.anderson.msfraud.model.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

    boolean existsByCustomerId(String customerId);
    boolean existsByCpf(String cpf);
}
