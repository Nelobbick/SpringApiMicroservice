package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.BankCards;
import com.example.bankcards.entity.Users;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.service.AdminService;
import com.example.bankcards.service.BankCardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final BankCardService bankCardService;
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);


    public AdminController(AdminService adminService, BankCardService bankCardService) {
        this.adminService = adminService;
        this.bankCardService = bankCardService;
    }

    // ==================== Управление картами ====================

    /**
     * Создание новой карты
     */
    @PostMapping("/cards/create")
    public ResponseEntity<?> createCard(@RequestBody CreateCardDTO request) {
        log.info("Creating card for user ID: {}", request.getUserId());
        try {
            BankCards card = adminService.createCard(
                    request.getCardNumber(),
                    request.getExpiryDate(),
                    request.getUserId()
            );

            log.info("Card created successfully with ID: {}", card.getId());
            return ResponseEntity.ok(Map.of(
                    "message", "Card created successfully",
                    "cardId", card.getId()
            ));
        } catch (ValidationException e) {
            log.warn("Validation error while creating card: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating card", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create card: " + e.getMessage()));
        }
    }

    /**
     * Блокировка карты
     */
    @PutMapping("/cards/{cardId}/block")
    public ResponseEntity<?> blockCard(@PathVariable Long cardId) {
        log.info("Blocking card with ID: {}", cardId);
        try {
            BankCards card = adminService.blockCard(cardId);
            log.info("Card {} blocked successfully", cardId);
            return ResponseEntity.ok(Map.of(
                    "message", "Card blocked successfully",
                    "cardId", card.getId(),
                    "status", card.getStatus()
            ));
        } catch (CardNotFoundException e) {
            log.warn("Card not found for blocking: {}", cardId);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error blocking card {}", cardId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to block card: " + e.getMessage()));
        }
    }

    /**
     * Активация карты
     */
    @PutMapping("/cards/{cardId}/activate")
    public ResponseEntity<?> activateCard(@PathVariable Long cardId) {
        log.info("Activating card with ID: {}", cardId);
        try {
            BankCards card = adminService.activateCard(cardId);
            log.info("Card {} activated successfully", cardId);
            return ResponseEntity.ok(Map.of(
                    "message", "Card activated successfully",
                    "cardId", card.getId(),
                    "status", card.getStatus()
            ));
        } catch (CardNotFoundException e) {
            log.warn("Card not found for activation: {}", cardId);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error activating card {}", cardId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to activate card: " + e.getMessage()));
        }
    }

    /**
     * Удаление карты
     */
    @DeleteMapping("/cards/{cardId}/delete")
    public ResponseEntity<?> deleteCard(@PathVariable Long cardId) {
        log.info("Deleting card with ID: {}", cardId);
        try {
            adminService.deleteCard(cardId);
            log.info("Card {} deleted successfully", cardId);
            return ResponseEntity.ok(Map.of("message", "Card deleted successfully"));
        } catch (CardNotFoundException e) {
            log.warn("Card not found for deletion: {}", cardId);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting card {}", cardId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to delete card: " + e.getMessage()));
        }
    }

    /**
     * Получение всех карт
     */
    @GetMapping("/cards")
    public ResponseEntity<?> getAllCards() {
        log.info("Getting all cards");
        try {
            List<BankCards> cards = bankCardService.getAllCards();
            log.info("Retrieved cards");
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            log.error("Error retrieving cards", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve cards: " + e.getMessage()));
        }
    }

    /**
     * Получение карты по ID
     */
    @GetMapping("/cards/{cardId}")
    public ResponseEntity<?> getCardById(@PathVariable Long cardId) {
        log.info("Getting card by ID: {}", cardId);
        try {
            Optional<BankCards> card = bankCardService.getCardById(cardId);
            if (card.isPresent()) {
                log.info("Card {} found", cardId);
                return ResponseEntity.ok(card.get());
            } else {
                log.warn("Card {} not found", cardId);
                return ResponseEntity.badRequest().body(Map.of("error", "Card not found"));
            }
        } catch (Exception e) {
            log.error("Error getting card {}", cardId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Card not found: " + e.getMessage()));
        }
    }

    /**
     * Установка баланса карты по ID карты
     */
    @PutMapping("/cards/{cardId}/setBalanceByCardId")
    public ResponseEntity<?> setBalanceById(@PathVariable Long cardId,@Valid @RequestBody SetBalanceByIdDTO setBalanceDTO) {
        log.info("Setting balance for card ID: {} to {}", cardId, setBalanceDTO.getBalance());
        try {
            BankCards cards = adminService.setCardBalance(cardId, setBalanceDTO.getBalance());
            log.info("Balance set successfully for card {}", cardId);
            return ResponseEntity.ok(Map.of(
                    "message", "Balance set successfully",
                    "cardId", cards.getId(),
                    "newBalance", cards.getBalance()
            ));
        } catch (CardNotFoundException e) {
            log.warn("Card not found for balance setting: {}", cardId);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error setting balance for card {}", cardId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to set card balance"));
        }
    }

    /**
     * Установка баланса карты по номеру карты
     */
    @PutMapping("/cards/setBalanceByCardNumber")
    public ResponseEntity<?> setBalanceByCardNumber(@Valid @RequestBody SetBalanceByCardNumber setBalanceByCardNumber) {
        log.info("Setting balance for card number: {} to {}",
                setBalanceByCardNumber.getCardNumber(), setBalanceByCardNumber.getBalance());
        try {
            BankCards card = adminService.setCardBalance(
                    setBalanceByCardNumber.getCardNumber(),
                    setBalanceByCardNumber.getBalance()
            );
            log.info("Balance set successfully for card number {}", setBalanceByCardNumber.getCardNumber());
            return ResponseEntity.ok(Map.of(
                    "message", "Balance set successfully",
                    "cardNumber", card.getCardNumber(),
                    "newBalance", card.getBalance()
            ));
        } catch (CardNotFoundException e) {
            log.warn("Card not found for balance setting: {}", setBalanceByCardNumber.getCardNumber());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error setting balance for card number {}", setBalanceByCardNumber.getCardNumber(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to set card balance"));
        }
    }

    // ==================== Управление пользователями ====================

    /**
     * Создание нового пользователя
     */
    @PostMapping("/users/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDTO request) {
        log.info("Creating user with username: {}", request.getUsername());
        try {
            Users user = adminService.createUser(
                    request.getUsername(),
                    request.getPassword(),
                    "ROLE_"+request.getRole()
            );
            log.info("User {} created successfully with ID: {}", user.getUsername(), user.getId());
            return ResponseEntity.ok(Map.of(
                    "message", "User created successfully",
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "role", user.getRole()
            ));
        } catch (ValidationException e) {
            log.warn("Validation error while creating user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create user: " + e.getMessage()));
        }
    }

    /**
     * Получение всех пользователей
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        log.info("Getting all users");
        try {
            List<Users> users = adminService.getAllUsers();
            log.info("Retrieved {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve users: " + e.getMessage()));
        }
    }

    /**
     * Получение пользователя по ID
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        log.info("Getting user by ID: {}", userId);
        try {
            Optional<Users> user = adminService.getUserById(userId);
            if (user.isPresent()) {
                log.info("User {} found", userId);
                return ResponseEntity.ok(user.get());
            } else {
                log.warn("User {} not found", userId);
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
        } catch (Exception e) {
            log.error("Error getting user {}", userId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "User not found: " + e.getMessage()));
        }
    }

    /**
     * Обновление пользователя
     */
    @PutMapping("/users/{userId}/update")
    public ResponseEntity<?> updateUser(@PathVariable Long userId,@Valid @RequestBody UpdateUserDTO request) {
        log.info("Updating user ID: {}", userId);
        try {
            Users user = adminService.updateUser(userId, request.getUsername(), request.getRole());
            log.info("User {} updated successfully", userId);
            return ResponseEntity.ok(Map.of(
                    "message", "User updated successfully",
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "role", user.getRole()
            ));
        } catch (UserNotFoundException e) {
            log.warn("User not found for update: {}", userId);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (ValidationException e) {
            log.warn("Validation error while updating user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating user {}", userId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update user: " + e.getMessage()));
        }
    }

    /**
     * Удаление пользователя
     */
    @DeleteMapping("/users/{userId}/delete")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            adminService.deleteUser(userId);
            log.info("User {} deleted successfully", userId);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (UserNotFoundException e) {
            log.warn("User not found for deletion: {}", userId);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting user {}", userId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to delete user: " + e.getMessage()));
        }
    }
}