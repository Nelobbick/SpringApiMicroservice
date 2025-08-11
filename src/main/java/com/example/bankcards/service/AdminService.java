package com.example.bankcards.service;

import com.example.bankcards.entity.BankCards;
import com.example.bankcards.entity.Users;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.util.CardMaskingUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final BankCardRepository bankCardRepository;
    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CardMaskingUtil cardMaskingUtil;

    public AdminService(BankCardRepository bankCardRepository, UsersRepository userRepository, PasswordEncoder passwordEncoder, CardMaskingUtil cardMaskingUtil) {
        this.bankCardRepository = bankCardRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cardMaskingUtil = cardMaskingUtil;
    }

    /**
     * Создание новой карты
     */
    public BankCards createCard(String cardNumber, String expiryDate, Long userId) {

        // Проверка существования пользователя
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        // Проверка уникальности номера карты
        if (bankCardRepository.existsByCardNumber(cardNumber)){
            throw new ValidationException("Card with this number already exists");
        }

        BankCards card = new BankCards();
        card.setCardNumber(cardNumber);
        card.setMasked_card_number(cardMaskingUtil.maskCardNumberWithX(cardNumber));
        card.setExpiryDate(LocalDate.parse(expiryDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        card.setBalance(BigDecimal.ZERO);
        card.setStatus("ACTIVE");
        card.setUser(user);

        return bankCardRepository.save(card);
    }

    /**
     * Блокировка карты
     */
    public BankCards blockCard(Long cardId) {
        BankCards card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        card.setStatus("BLOCKED");
        return bankCardRepository.save(card);
    }

    /**
     * Активация карты
     */
    public BankCards activateCard(Long cardId) {
        BankCards card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        card.setStatus("ACTIVE");
        return bankCardRepository.save(card);
    }

    /**
     * Удаление карты
     */
    public void deleteCard(Long cardId) {
        if (!bankCardRepository.existsById(cardId)) {
            throw new CardNotFoundException("Card not found");
        }
        bankCardRepository.deleteById(cardId);
    }

    /**
     * Создание пользователя
     */
    public Users createUser(String username, String password, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new ValidationException("User with this username already exists");
        }

        Users user = new Users();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role.toUpperCase());

        return userRepository.save(user);
    }

    /**
     * Обновление пользователя
     */
    public Users updateUser(Long userId, String username, String role) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new ValidationException("User with this username already exists");
        }

        user.setUsername(username);
        user.setRole(role.toUpperCase());

        return userRepository.save(user);
    }

    /**
     * Удаление пользователя
     */
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(userId);
    }

    /**
     * Установка баланса карты
     */
    public BankCards setCardBalance(Long cardId, BigDecimal balance) {
        BankCards card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        card.setBalance(balance);
        return bankCardRepository.save(card);
    }
    public BankCards setCardBalance(String cardNumber, BigDecimal balance) {
        BankCards card = bankCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        card.setBalance(balance);
        return bankCardRepository.save(card);
    }
    /**
     * Получение всех пользователей
     */
    public List<Users> getAllUsers(){
        return userRepository.findAll();
    }

    /**
     * Получение пользователя по ID
     */
    public Optional<Users> getUserById(Long id){
        return userRepository.findUsersById(id);
    }

}
