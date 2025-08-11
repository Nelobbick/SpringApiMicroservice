package com.example.bankcards.service;

import com.example.bankcards.entity.BankCards;
import com.example.bankcards.entity.Users;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {


    private final BankCardRepository bankCardRepository;
    private final UsersRepository userRepository;

    public UserService(BankCardRepository bankCardRepository, UsersRepository userRepository) {
        this.bankCardRepository = bankCardRepository;
        this.userRepository = userRepository;
    }

    /**
     * Получение всех карт пользователя
     */
    public List<BankCards> getAllUserCards(Long userId) {
        return bankCardRepository.findByUserId(userId);
    }

    /**
     * Получение всех карт пользователя с пагинацией
     */
    public Page<BankCards> getUserCardsPaginated(Long userId, Pageable pageable) {
        return bankCardRepository.findByUserId(userId, pageable);
    }

    /**
     * Получение активных карт пользователя
     */
    public List<BankCards> getActiveUserCards(Long userId) {
        return bankCardRepository.findByUserIdAndStatus(userId, "ACTIVE");
    }

    /**
     * Блокировка своей карты
     */
    public BankCards blockOwnCard(Long userId, Long cardId) {
        BankCards card = bankCardRepository.findByUserIdAndId(userId, cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found or access denied"));

        if (!card.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied: This is not your card");
        }

        card.setStatus("BLOCKED");

        return bankCardRepository.save(card);
    }

    /**
     * Перевод средств между своими картами
     */
    public void transferBetweenOwnCards(Long userId, Long sourceCardId, Long targetCardId, BigDecimal amount) {
        if (sourceCardId.equals(targetCardId)) {
            throw new RuntimeException("Cannot transfer to the same card");
        }

        BankCards sourceCard = bankCardRepository.findByUserIdAndId(userId, sourceCardId)
                .orElseThrow(() -> new CardNotFoundException("Source card not found or access denied"));

        BankCards targetCard = bankCardRepository.findByUserIdAndId(userId, targetCardId)
                .orElseThrow(() -> new CardNotFoundException("Source card not found or access denied"));

        if (!sourceCard.getStatus().equals("ACTIVE")) {
            throw new RuntimeException("Source card is not active");
        }

        if (!targetCard.getStatus().equals("ACTIVE")) {
            throw new RuntimeException("Target card is not active");
        }

        if (sourceCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds on source card");
        }

        sourceCard.setBalance(sourceCard.getBalance().subtract(amount));
        targetCard.setBalance(targetCard.getBalance().add(amount));

        bankCardRepository.save(sourceCard);
        bankCardRepository.save(targetCard);
    }

    /**
     * Получение баланса своей карты
     */
    public BigDecimal getCardBalance(Long userId, Long cardId) {
        BankCards card = bankCardRepository.findByUserIdAndId(userId, cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found or access denied"));

        return card.getBalance();
    }

    /**
     * Получение информации о пользователе
     */
    public Users getUserInfo(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
    public Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return getUserIdByUsername(username);
        } else {
            throw new RuntimeException("Invalid authentication object");
        }
    }
    private Long getUserIdByUsername(String username) {
        Optional<Users> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            return userOptional.get().getId();
        } else {
            throw new UserNotFoundException("User not found with username: " + username);
        }
    }
    public BigDecimal getTotalBalanceByUserId(Long userId) {

        // Вариант 1: Используем репозиторий (если есть готовый метод)
        BigDecimal totalBalance = bankCardRepository.sumBalanceByUserId(userId);

        // Если в БД NULL, возвращаем 0
        return totalBalance != null ? totalBalance : BigDecimal.ZERO;
    }
}