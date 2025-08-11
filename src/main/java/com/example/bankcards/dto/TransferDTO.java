package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class TransferDTO {
    @NotNull(message = "Source card ID is required")
    @Positive(message = "Source card ID must be positive")
    private Long sourceCardId;

    @NotNull(message = "Target card ID is required")
    @Positive(message = "Target card ID must be positive")
    private Long targetCardId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public Long getSourceCardId() {
        return sourceCardId;
    }

    public Long getTargetCardId() {
        return targetCardId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setSourceCardId(Long sourceCardId) {
        this.sourceCardId = sourceCardId;
    }

    public void setTargetCardId(Long targetCardId) {
        this.targetCardId = targetCardId;
    }
}