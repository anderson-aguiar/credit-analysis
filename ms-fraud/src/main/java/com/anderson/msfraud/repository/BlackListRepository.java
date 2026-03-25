package com.anderson.msfraud.repository;

import com.anderson.msfraud.model.BlackList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListRepository extends JpaRepository<BlackList, Long> {

    boolean existsByCustomerId(String customerId);
    boolean existsByCpf(String cpf);
}
