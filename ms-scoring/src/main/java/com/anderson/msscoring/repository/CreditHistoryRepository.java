package com.anderson.msscoring.repository;

import com.anderson.msscoring.model.CreditHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CreditHistoryRepository extends MongoRepository<CreditHistory, String> {

    Optional<CreditHistory> findByCustomerId(String customerId);
}
