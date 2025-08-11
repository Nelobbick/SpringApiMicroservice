package com.example.bankcards.util;
import com.example.bankcards.entity.BankCards;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class BankCardsValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return BankCards.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        BankCards card = (BankCards) target;

        // Проверка cardNumber
        if (card.getCardNumber() == null || card.getCardNumber().isEmpty()) {
            errors.rejectValue("cardNumber", "required", "Card number is required");
        } else if (!isValidCardNumber(card.getCardNumber())) {
            errors.rejectValue("cardNumber", "invalid", "Invalid card number format");
        }

        // Проверка expiryDate
        if (card.getExpiryDate() == null) {
            errors.rejectValue("expiryDate", "required", "Expiry date is required");
        } else if (card.getExpiryDate().isBefore(LocalDate.now())) {
            errors.rejectValue("expiryDate", "expired", "Expiry date cannot be in the past");
        }

        // Проверка balance
        if (card.getBalance() == null) {
            errors.rejectValue("balance", "required", "Balance is required");
        } else if (card.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            errors.rejectValue("balance", "negative", "Balance cannot be negative");
        }

        // Проверка status
        if (card.getStatus() == null || card.getStatus().isEmpty()) {
            errors.rejectValue("status", "required", "Status is required");
        } else if (!isValidStatus(card.getStatus())) {
            errors.rejectValue("status", "invalid", "Invalid status. Allowed values: ACTIVE, BLOCKED, EXPIRED");
        }

        // Проверка userId
        if (card.getUserId() == null) {
            errors.rejectValue("userId", "required", "User ID is required");
        }
    }

    private boolean isValidCardNumber(String cardNumber) {
        // Простая проверка формата (16 цифр)
        return cardNumber.matches("^\\d{16}$");
    }

    private boolean isValidStatus(String status) {
        return "ACTIVE".equals(status) || "BLOCKED".equals(status);
    }
}