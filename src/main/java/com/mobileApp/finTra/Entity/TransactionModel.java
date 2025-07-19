package com.mobileApp.finTra.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class TransactionModel {
    @Id
    @GeneratedValue
    private Long id;
    private String reference;
    @Column(name = "user_id")
    private Long userId;
    private String type; // "TOPUP", "WITHDRAWAL", "TRANSFER"
    private int amount;
    private String status; // "PENDING", "SUCCESS", "FAILED"

    private LocalDateTime createdAt = LocalDateTime.now();

    public TransactionModel(String reference, Long userId, String topup, int amount, String pending) {
        this.reference = reference;
        this.userId = userId;
        this.type = topup;
        this.amount = amount;
        this.status = pending;
    }

    public TransactionModel() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
