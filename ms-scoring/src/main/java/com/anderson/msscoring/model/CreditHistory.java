package com.anderson.msscoring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;


@Document(collection = "credit_history")
public class CreditHistory {

    @Id
    private String id;
    @Indexed
    private String customerId;

    private BigDecimal totalAmount;
    private Integer totalInstallments;
    private Integer latePayments;
    private LocalDateTime lastCreditDate;
    private Integer relationshipMonths;

    public CreditHistory(){}
    public CreditHistory(String id, String customerId, BigDecimal totalAmount,
                         Integer totalInstallments, Integer latePayments,
                         LocalDateTime lastCreditDate, Integer relationshipMonths) {
        this.id = id;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.totalInstallments = totalInstallments;
        this.latePayments = latePayments;
        this.lastCreditDate = lastCreditDate;
        this.relationshipMonths = relationshipMonths;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    public void setTotalInstallments(Integer totalInstallments) {
        this.totalInstallments = totalInstallments;
    }

    public Integer getLatePayments() {
        return latePayments;
    }

    public void setLatePayments(Integer latePayments) {
        this.latePayments = latePayments;
    }

    public LocalDateTime getLastCreditDate() {
        return lastCreditDate;
    }

    public void setLastCreditDate(LocalDateTime lastCreditDate) {
        this.lastCreditDate = lastCreditDate;
    }

    public Integer getRelationshipMonths() {
        return relationshipMonths;
    }

    public void setRelationshipMonths(Integer relationshipMonths) {
        this.relationshipMonths = relationshipMonths;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CreditHistory that = (CreditHistory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
