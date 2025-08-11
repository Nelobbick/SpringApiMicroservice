package com.example.bankcards.util;

import com.example.bankcards.entity.BankCards;
import com.example.bankcards.repository.BankCardRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class CardExpirationChecker {

    private final BankCardRepository bankCardRepository;

    public CardExpirationChecker(BankCardRepository bankCardRepository) {
        this.bankCardRepository = bankCardRepository;
    }

    // Проверяем истекшие карты каждый день в 02:00
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkExpiredCards() {

        // Находим все активные карты с истекшим сроком
        List<BankCards> expiredCards = bankCardRepository.findActiveExpiredCards(LocalDate.now());

        if (!expiredCards.isEmpty()) {

            for (BankCards card : expiredCards) {
                card.setStatus("EXPIRED");
                bankCardRepository.save(card);

            }
        }

    }
}