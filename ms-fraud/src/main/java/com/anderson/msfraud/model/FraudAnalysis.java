package com.anderson.msfraud.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_analysis")
public class FraudAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String requestId;

    @Column(nullable = false)
    private String customerId;

    private String cpf;
    private BigDecimal amount;
    private BigDecimal declaredIncome;
    private Boolean documentValid;
    private Boolean incomeValid;
    private Boolean blacklistValid;
    private Boolean behaviorValid;
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    private FraudStatus finalDecision;

    @Column(length = 500)
    private String reason;
    LocalDateTime analyzedAt;

    public FraudAnalysis() {
    }

    public FraudAnalysis(Long id, String requestId, String customerId, String cpf, BigDecimal amount,
                         BigDecimal declaredIncome, Boolean documentValid, Boolean incomeValid, Boolean blacklistValid,
                         Boolean behaviorValid, Integer riskScore, FraudStatus finalDecision,
                         String reason, LocalDateTime analyzedAt) {
        this.id = id;
        this.requestId = requestId;
        this.customerId = customerId;
        this.cpf = cpf;
        this.amount = amount;
        this.declaredIncome = declaredIncome;
        this.documentValid = documentValid;
        this.incomeValid = incomeValid;
        this.blacklistValid = blacklistValid;
        this.behaviorValid = behaviorValid;
        this.riskScore = riskScore;
        this.finalDecision = finalDecision;
        this.reason = reason;
        this.analyzedAt = analyzedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getDeclaredIncome() {
        return declaredIncome;
    }

    public void setDeclaredIncome(BigDecimal declaredIncome) {
        this.declaredIncome = declaredIncome;
    }

    public Boolean getDocumentValid() {
        return documentValid;
    }

    public void setDocumentValid(Boolean documentValid) {
        this.documentValid = documentValid;
    }

    public Boolean getIncomeValid() {
        return incomeValid;
    }

    public void setIncomeValid(Boolean incomeValid) {
        this.incomeValid = incomeValid;
    }

    public Boolean getBlacklistValid() {
        return blacklistValid;
    }

    public void setBlacklistValid(Boolean blacklistValid) {
        this.blacklistValid = blacklistValid;
    }

    public Boolean getBehaviorValid() {
        return behaviorValid;
    }

    public void setBehaviorValid(Boolean behaviorValid) {
        this.behaviorValid = behaviorValid;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public FraudStatus getFinalDecision() {
        return finalDecision;
    }

    public void setFinalDecision(FraudStatus finalDecision) {
        this.finalDecision = finalDecision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}
