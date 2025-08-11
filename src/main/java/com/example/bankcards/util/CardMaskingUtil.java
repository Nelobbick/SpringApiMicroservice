package com.example.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardMaskingUtil {

    // 1234XXXXXXXX3456
    public String maskCardNumberWithX(String cardNumber) {
        String cleanCardNumber = cardNumber.replaceAll("[^0-9]", "");

        if (cleanCardNumber.length() < 8) {
            throw new IllegalArgumentException("Некорректный номер карты");
        }

        String firstFour = cleanCardNumber.substring(0, 4);
        String lastFour = cleanCardNumber.substring(cleanCardNumber.length() - 4);
        String mask = "X".repeat(cleanCardNumber.length() - 8);

        return firstFour + mask + lastFour;
    }
}