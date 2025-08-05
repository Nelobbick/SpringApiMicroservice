package com.example.bankcards.entity;

import com.example.bankcards.entity.Users;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bank_cards",
        indexes = {
                @Index(name = "idx_bank_cards_card_number", columnList = "card_number"),
                @Index(name = "idx_bank_cards_user_id", columnList = "user_id"),
                @Index(name = "idx_bank_cards_status", columnList = "status")
        })
public class BankCards {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", unique = true, nullable = false, length = 16)
    private String cardNumber;

    @Column(name = "masked_number", nullable = false, length = 19)
    private String maskedNumber;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    // Связь ManyToOne с таблицей users
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    public Long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getMaskedNumber() {
        return maskedNumber;
    }

    public String getStatus() {
        return status;
    }

    public Users getUser() {
        return user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setMaskedNumber(String maskedNumber) {

    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}