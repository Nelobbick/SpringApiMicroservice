package com.example.bankcards.service;

import com.example.bankcards.entity.BankCards;
import com.example.bankcards.entity.Users;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.util.CardMaskingUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @Mock
    private BankCardRepository bankCardRepository;

    @Mock
    private UsersRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CardMaskingUtil cardMaskingUtil;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- Тесты для карт ---

    @Test
    void testCreateCard_Success() {
        Long userId = 1L;
        String cardNumber = "1234567890123456";
        String expiryDateStr = "2025-12-31";
        String maskedCardNumber = "1234XXXXXXXX3456";

        Users mockUser = new Users("testuser", "password", "ROLE_USER");
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(bankCardRepository.existsByCardNumber(cardNumber)).thenReturn(false);
        when(cardMaskingUtil.maskCardNumberWithX(cardNumber)).thenReturn(maskedCardNumber);

        BankCards savedCard = new BankCards();
        savedCard.setId(10L);
        savedCard.setCardNumber(cardNumber);
        savedCard.setMasked_card_number(maskedCardNumber);
        savedCard.setExpiryDate(LocalDate.parse(expiryDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        savedCard.setBalance(BigDecimal.ZERO);
        savedCard.setStatus("ACTIVE");
        savedCard.setUser(mockUser);
        savedCard.setUserId(userId);

        when(bankCardRepository.save(any(BankCards.class))).thenReturn(savedCard);

        BankCards result = adminService.createCard(cardNumber, expiryDateStr, userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getCardNumber()).isEqualTo(cardNumber);
        assertThat(result.getMasked_card_number()).isEqualTo(maskedCardNumber);
        assertThat(result.getExpiryDate()).isEqualTo(LocalDate.parse(expiryDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getUserId()).isEqualTo(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(bankCardRepository, times(1)).existsByCardNumber(cardNumber);
        verify(cardMaskingUtil, times(1)).maskCardNumberWithX(cardNumber);
        verify(bankCardRepository, times(1)).save(any(BankCards.class));
    }

    @Test
    void testCreateCard_UserNotFound() {
        Long userId = 999L;
        String cardNumber = "1234567890123456";
        String expiryDateStr = "2025-12-31";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.createCard(cardNumber, expiryDateStr, userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, times(1)).findById(userId);
        verify(bankCardRepository, never()).existsByCardNumber(anyString());
        verify(bankCardRepository, never()).save(any(BankCards.class));
    }

    @Test
    void testCreateCard_CardAlreadyExists() {
        Long userId = 1L;
        String cardNumber = "1234567890123456";
        String expiryDateStr = "2025-12-31";

        Users mockUser = new Users("testuser", "password", "ROLE_USER");
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(bankCardRepository.existsByCardNumber(cardNumber)).thenReturn(true);

        assertThatThrownBy(() -> adminService.createCard(cardNumber, expiryDateStr, userId))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Card with this number already exists");

        verify(userRepository, times(1)).findById(userId);
        verify(bankCardRepository, times(1)).existsByCardNumber(cardNumber);
        verify(bankCardRepository, never()).save(any(BankCards.class));
    }


    @Test
    void testBlockCard_Success() {
        Long cardId = 1L;
        BankCards mockCard = new BankCards();
        mockCard.setId(cardId);
        mockCard.setStatus("ACTIVE");

        when(bankCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(bankCardRepository.save(mockCard)).thenReturn(mockCard);

        BankCards result = adminService.blockCard(cardId);

        assertThat(result.getStatus()).isEqualTo("BLOCKED");
        verify(bankCardRepository, times(1)).findById(cardId);
        verify(bankCardRepository, times(1)).save(mockCard);
    }

    @Test
    void testBlockCard_NotFound() {
        Long cardId = 999L;
        when(bankCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.blockCard(cardId))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Card not found");

        verify(bankCardRepository, times(1)).findById(cardId);
        verify(bankCardRepository, never()).save(any(BankCards.class));
    }

    @Test
    void testActivateCard_Success() {
        Long cardId = 1L;
        BankCards mockCard = new BankCards();
        mockCard.setId(cardId);
        mockCard.setStatus("BLOCKED");

        when(bankCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(bankCardRepository.save(mockCard)).thenReturn(mockCard);

        BankCards result = adminService.activateCard(cardId);

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(bankCardRepository, times(1)).findById(cardId);
        verify(bankCardRepository, times(1)).save(mockCard);
    }

    @Test
    void testDeleteCard_Success() {
        Long cardId = 1L;
        when(bankCardRepository.existsById(cardId)).thenReturn(true);

        adminService.deleteCard(cardId);

        verify(bankCardRepository, times(1)).existsById(cardId);
        verify(bankCardRepository, times(1)).deleteById(cardId);
    }

    @Test
    void testDeleteCard_NotFound() {
        Long cardId = 999L;
        when(bankCardRepository.existsById(cardId)).thenReturn(false);

        assertThatThrownBy(() -> adminService.deleteCard(cardId))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Card not found");

        verify(bankCardRepository, times(1)).existsById(cardId);
        verify(bankCardRepository, never()).deleteById(anyLong());
    }

    @Test
    void testSetCardBalance_ById_Success() {
        Long cardId = 1L;
        BigDecimal newBalance = new BigDecimal("500.00");
        BankCards mockCard = new BankCards();
        mockCard.setId(cardId);
        mockCard.setBalance(BigDecimal.ZERO);

        when(bankCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(bankCardRepository.save(mockCard)).thenReturn(mockCard);

        BankCards result = adminService.setCardBalance(cardId, newBalance);

        assertThat(result.getBalance()).isEqualByComparingTo(newBalance);
        verify(bankCardRepository, times(1)).findById(cardId);
        verify(bankCardRepository, times(1)).save(mockCard);
    }

    @Test
    void testSetCardBalance_ByCardNumber_Success() {
        String cardNumber = "1234567890123456";
        BigDecimal newBalance = new BigDecimal("750.00");
        BankCards mockCard = new BankCards();
        mockCard.setCardNumber(cardNumber);
        mockCard.setBalance(BigDecimal.ZERO);

        when(bankCardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(mockCard));
        when(bankCardRepository.save(mockCard)).thenReturn(mockCard);

        BankCards result = adminService.setCardBalance(cardNumber, newBalance);

        assertThat(result.getBalance()).isEqualByComparingTo(newBalance);
        verify(bankCardRepository, times(1)).findByCardNumber(cardNumber);
        verify(bankCardRepository, times(1)).save(mockCard);
    }

    // --- Тесты для пользователей ---

    @Test
    void testCreateUser_Success() {
        String username = "newuser";
        String password = "plaintextpassword";
        String role = "USER";
        String encodedPassword = "encodedpassword";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        Users savedUser = new Users();
        savedUser.setId(2L);
        savedUser.setUsername(username);
        savedUser.setPassword(encodedPassword);
        savedUser.setRole("ROLE_USER");

        when(userRepository.save(any(Users.class))).thenReturn(savedUser);

        Users result = adminService.createUser(username, password, role);

        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getRole()).isEqualTo("ROLE_USER"); // Проверяем, что префикс ROLE_ добавлен
        verify(userRepository, times(1)).existsByUsername(username);
        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(Users.class));
    }

    @Test
    void testCreateUser_AlreadyExists() {
        String username = "existinguser";
        String password = "password";
        String role = "USER";

        when(userRepository.existsByUsername(username)).thenReturn(true);

        assertThatThrownBy(() -> adminService.createUser(username, password, role))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User with this username already exists");

        verify(userRepository, times(1)).existsByUsername(username);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    void testUpdateUser_Success() {
        Long userId = 1L;
        String newUsername = "updateduser";
        String newRole = "ROLE_ADMIN";
        Users mockUser = new Users("olduser", "password", "ROLE_USER");
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        Users result = adminService.updateUser(userId, newUsername, newRole);

        assertThat(result.getUsername()).isEqualTo(newUsername);
        assertThat(result.getRole()).isEqualTo("ROLE_ADMIN"); // Проверяем префикс
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByUsername(newUsername);
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void testUpdateUser_NotFound() {
        Long userId = 999L;
        String newUsername = "updateduser";
        String newRole = "ADMIN";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateUser(userId, newUsername, newRole))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    void testUpdateUser_UsernameAlreadyExists() {
        Long userId = 1L;
        String newUsername = "existinguser"; // Имя, которое уже занято
        String newRole = "ADMIN";
        Users mockUser = new Users("olduser", "password", "ROLE_USER");
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        // Проверяем, что новое имя отличается от старого и уже существует
        when(userRepository.existsByUsername(newUsername)).thenReturn(true);

        assertThatThrownBy(() -> adminService.updateUser(userId, newUsername, newRole))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User with this username already exists");

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByUsername(newUsername);
        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    void testDeleteUser_Success() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        adminService.deleteUser(userId);

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void testDeleteUser_NotFound() {
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> adminService.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).deleteById(anyLong());
    }
}