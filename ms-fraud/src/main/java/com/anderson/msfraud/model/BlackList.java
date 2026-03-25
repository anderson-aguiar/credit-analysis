package com.anderson.msfraud.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blacklist")
public class BlackList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true, nullable = false) String customerId;
    @Column(unique = true) String cpf;
    @Column(nullable = false, length = 255) String reason;
    LocalDateTime addedAt;

    public BlackList() {
    }

    public BlackList(Long id, String customerId, String cpf, String reason,
                     LocalDateTime addedAt) {
        this.id = id;
        this.customerId = customerId;
        this.cpf = cpf;
        this.reason = reason;
        this.addedAt = addedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
