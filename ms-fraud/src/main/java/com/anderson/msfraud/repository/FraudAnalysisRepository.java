package com.anderson.msfraud.repository;

import com.anderson.msfraud.model.FraudAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FraudAnalysisRepository extends JpaRepository<FraudAnalysis, Long> {

    Optional<FraudAnalysis> findByRequestId(String requestId);

    List<FraudAnalysis> findByCustomerId(String customerId);

}
