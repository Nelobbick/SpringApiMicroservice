package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferDTO;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.bankcards.entity.BankCards;
import com.example.bankcards.entity.Users;
import com.example.bankcards.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Получение всех карт пользователя
     */
    @GetMapping("/cards")
    public ResponseEntity<?> getUserCards(Authentication authentication) {
        log.info("Getting all user cards");
        try {
            Long userId = userService.getCurrentUserId(authentication);
            List<BankCards> cards = userService.getAllUserCards(userId);
            log.info("Retrieved {} cards for user {}", cards.size(), userId);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            log.error("Error getting user cards", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get cards: " + e.getMessage()));
        }
    }

    /**
     * Получение карт пользователя с пагинацией
     */
    @GetMapping("/cards/paginated")
    public ResponseEntity<?> getUserCardsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        log.info("Getting user cards with pagination - page: {}, size: {}", page, size);
        try {
            Long userId = userService.getCurrentUserId(authentication);
            Pageable pageable = PageRequest.of(page, size);
            Page<BankCards> cards = userService.getUserCardsPaginated(userId, pageable);
            log.info("Retrieved {} cards for user {} (page {})", cards.getNumberOfElements(), userId, page);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            log.error("Error getting paginated user cards", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get cards: " + e.getMessage()));
        }
    }

    /**
     * Получение только активных карт пользователя
     */
    @GetMapping("/cards/active")
    public ResponseEntity<?> getActiveUserCards(Authentication authentication) {
        log.info("Getting active user cards");
        try {
            Long userId = userService.getCurrentUserId(authentication);
            List<BankCards> cards = userService.getActiveUserCards(userId);
            log.info("Retrieved {} active cards for user {}", cards.size(), userId);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            log.error("Error getting active user cards", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get active cards: " + e.getMessage()));
        }
    }

    /**
     * Блокировка своей карты
     */
    @PutMapping("/cards/{cardId}/block")
    public ResponseEntity<?> blockOwnCard(@PathVariable Long cardId, Authentication authentication) {
        log.info("Blocking own card with ID: {}", cardId);
        try {
            Long userId = userService.getCurrentUserId(authentication);
            BankCards card = userService.blockOwnCard(userId, cardId);
            log.info("Card {} blocked successfully by user {}", cardId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "Card blocked successfully",
                    "cardId", card.getId(),
                    "status", card.getStatus()
            ));
        } catch (CardNotFoundException e) {
            log.warn("Card not found for blocking: {}", cardId);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error blocking card {} by user", cardId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to block card: " + e.getMessage()));
        }
    }

    /**
     * Перевод средств между своими картами
     */
    @PostMapping("/transfer")
    public ResponseEntity<?> transferBetweenCards(
            @Valid @RequestBody TransferDTO transferDTO,
            Authentication authentication) {
        log.info("Transferring {} from card {} to card {}",
                transferDTO.getAmount(), transferDTO.getSourceCardId(), transferDTO.getTargetCardId());
        try {
            Long userId = userService.getCurrentUserId(authentication);
            userService.transferBetweenOwnCards(userId, transferDTO.getSourceCardId(), transferDTO.getTargetCardId(), transferDTO.getAmount());
            log.info("Transfer completed successfully");
            return ResponseEntity.ok(Map.of(
                    "message", "Transfer completed successfully",
                    "sourceCardId", transferDTO.getSourceCardId(),
                    "targetCardId", transferDTO.getTargetCardId(),
                    "amount", transferDTO.getAmount()
            ));
        } catch (CardNotFoundException e) {
            log.warn("Card not found during transfer: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (InsufficientFundsException e) {
            log.warn("Insufficient funds during transfer: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (ValidationException e) {
            log.warn("Validation error during transfer: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error during transfer", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Transfer failed: " + e.getMessage()));
        }
    }

    /**
     * Получение баланса карты
     */
    @GetMapping("/cards/{cardId}/balance")
    public ResponseEntity<?> getCardBalance(@PathVariable Long cardId, Authentication authentication) {
        log.info("Getting balance for card ID: {}", cardId);
        try {
            Long userId = userService.getCurrentUserId(authentication);
            BigDecimal balance = userService.getCardBalance(userId, cardId);
            log.info("Balance retrieved for card {}: {}", cardId, balance);
            return ResponseEntity.ok(Map.of(
                    "cardId", cardId,
                    "balance", balance
            ));
        } catch (CardNotFoundException e) {
            log.warn("Card not found for balance check: {}", cardId);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting balance for card {}", cardId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get balance: " + e.getMessage()));
        }
    }
    @GetMapping("/cards/total-balance")
    public ResponseEntity<?> getTotalBalance(Authentication authentication) {
        log.info("Getting total balance for user cards");
        try {
            Long userId = userService.getCurrentUserId(authentication);
            BigDecimal totalBalance = userService.getTotalBalanceByUserId(userId);
            log.info("Total balance retrieved for user {}: {}", userId, totalBalance);
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "totalBalance", totalBalance
            ));
        } catch (Exception e) {
            log.error("Error getting total balance for user", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get total balance: " + e.getMessage()));
        }
    }

    /**
     * Получение информации о пользователе
     */
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        log.info("Getting user info");
        try {
            Long userId = userService.getCurrentUserId(authentication);
            Users user = userService.getUserInfo(userId);
            log.info("User info retrieved for user {}", userId);
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "password", user.getPassword()
            ));
        } catch (UserNotFoundException e) {
            log.warn("User not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting user info", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to get user info: " + e.getMessage()));
        }
    }


}

