package com.example.bankcards.service;

import com.example.bankcards.entity.BankCards;
import com.example.bankcards.entity.Users;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private BankCardRepository bankCardRepository;

    @Mock
    private UsersRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- Тесты для получения ID пользователя из Authentication ---

    @Test
    void testGetCurrentUserId_Success() {
        String username = "testuser";
        Long userId = 1L;
        Users mockUser = new Users();
        mockUser.setId(userId);
        mockUser.setUsername(username);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);
        // Используем конструктор с authorities для создания полностью аутентифицированного токена
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList()); // или List.of()

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        Long resultUserId = userService.getCurrentUserId(authentication);

        assertThat(resultUserId).isEqualTo(userId);
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testGetCurrentUserId_UserNotFound() {
        String username = "nonexistent";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);
        // Используем конструктор с authorities
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList()); // или List.of()

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUserId(authentication))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with username: " + username);

        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testGetCurrentUserId_Unauthenticated() {
        assertThatThrownBy(() -> userService.getCurrentUserId(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not authenticated");
    }

    // --- Тесты для карт пользователя ---

    @Test
    void testGetAllUserCards() {
        Long userId = 1L;
        BankCards card1 = new BankCards();
        card1.setId(1L);
        card1.setUserId(userId);
        BankCards card2 = new BankCards();
        card2.setId(2L);
        card2.setUserId(userId);
        List<BankCards> cards = List.of(card1, card2);

        when(bankCardRepository.findByUserId(userId)).thenReturn(cards);

        List<BankCards> result = userService.getAllUserCards(userId);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(card1, card2);
        verify(bankCardRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testGetUserCardsPaginated() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 2);
        BankCards card1 = new BankCards();
        card1.setId(1L);
        card1.setUserId(userId);
        BankCards card2 = new BankCards();
        card2.setId(2L);
        card2.setUserId(userId);
        List<BankCards> cards = List.of(card1, card2);
        Page<BankCards> page = new PageImpl<>(cards, pageable, cards.size());

        when(bankCardRepository.findByUserId(userId, pageable)).thenReturn(page);

        Page<BankCards> result = userService.getUserCardsPaginated(userId, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(bankCardRepository, times(1)).findByUserId(userId, pageable);
    }

    @Test
    void testGetActiveUserCards() {
        Long userId = 1L;
        BankCards activeCard = new BankCards();
        activeCard.setId(1L);
        activeCard.setUserId(userId);
        activeCard.setStatus("ACTIVE");
        BankCards blockedCard = new BankCards();
        blockedCard.setId(2L);
        blockedCard.setUserId(userId);
        blockedCard.setStatus("BLOCKED");
        List<BankCards> activeCards = List.of(activeCard);

        when(bankCardRepository.findByUserIdAndStatus(userId, "ACTIVE")).thenReturn(activeCards);

        List<BankCards> result = userService.getActiveUserCards(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("ACTIVE");
        verify(bankCardRepository, times(1)).findByUserIdAndStatus(userId, "ACTIVE");
    }

    @Test
    void testBlockOwnCard_Success() {
        Long userId = 1L;
        Long cardId = 10L;
        BankCards mockCard = new BankCards();
        mockCard.setId(cardId);
        mockCard.setUserId(userId);
        mockCard.setStatus("ACTIVE");

        when(bankCardRepository.findByUserIdAndId(userId, cardId)).thenReturn(Optional.of(mockCard));
        when(bankCardRepository.save(mockCard)).thenReturn(mockCard);

        BankCards result = userService.blockOwnCard(userId, cardId);

        assertThat(result.getStatus()).isEqualTo("BLOCKED");
        verify(bankCardRepository, times(1)).findByUserIdAndId(userId, cardId);
        verify(bankCardRepository, times(1)).save(mockCard);
    }

    @Test
    void testBlockOwnCard_NotFound() {
        Long userId = 1L;
        Long cardId = 999L;

        when(bankCardRepository.findByUserIdAndId(userId, cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.blockOwnCard(userId, cardId))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Card not found or access denied");

        verify(bankCardRepository, times(1)).findByUserIdAndId(userId, cardId);
        verify(bankCardRepository, never()).save(any(BankCards.class));
    }

    // --- Тесты для перевода средств ---

    @Test
    void testTransferBetweenOwnCards_Success() {
        Long userId = 1L;
        Long sourceCardId = 10L;
        Long targetCardId = 20L;
        BigDecimal amount = new BigDecimal("50.00");

        BankCards sourceCard = new BankCards();
        sourceCard.setId(sourceCardId);
        sourceCard.setUserId(userId);
        sourceCard.setStatus("ACTIVE");
        sourceCard.setBalance(new BigDecimal("100.00"));

        BankCards targetCard = new BankCards();
        targetCard.setId(targetCardId);
        targetCard.setUserId(userId);
        targetCard.setStatus("ACTIVE");
        targetCard.setBalance(new BigDecimal("50.00"));

        when(bankCardRepository.findByUserIdAndId(userId, sourceCardId)).thenReturn(Optional.of(sourceCard));
        when(bankCardRepository.findByUserIdAndId(userId, targetCardId)).thenReturn(Optional.of(targetCard));

        userService.transferBetweenOwnCards(userId, sourceCardId, targetCardId, amount);

        assertThat(sourceCard.getBalance()).isEqualByComparingTo(new BigDecimal("50.00")); // 100 - 50
        assertThat(targetCard.getBalance()).isEqualByComparingTo(new BigDecimal("100.00")); // 50 + 50
        verify(bankCardRepository, times(1)).findByUserIdAndId(userId, sourceCardId);
        verify(bankCardRepository, times(1)).findByUserIdAndId(userId, targetCardId);
        verify(bankCardRepository, times(1)).save(sourceCard);
        verify(bankCardRepository, times(1)).save(targetCard);
    }

    @Test
    void testTransferBetweenOwnCards_SameCard() {
        Long userId = 1L;
        Long cardId = 10L;
        BigDecimal amount = new BigDecimal("50.00");

        assertThatThrownBy(() -> userService.transferBetweenOwnCards(userId, cardId, cardId, amount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot transfer to the same card");
    }

    @Test
    void testTransferBetweenOwnCards_SourceCardNotFound() {
        Long userId = 1L;
        Long sourceCardId = 999L; // ID, которого нет
        Long targetCardId = 20L;
        BigDecimal amount = new BigDecimal("50.00");

        // Настраиваем мок так, чтобы первый вызов (для source) возвращал empty
        when(bankCardRepository.findByUserIdAndId(userId, sourceCardId)).thenReturn(Optional.empty());
        // Нет необходимости настраивать мок для targetCardId, так как он не должен быть вызван

        assertThatThrownBy(() -> userService.transferBetweenOwnCards(userId, sourceCardId, targetCardId, amount))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Source card not found or access denied"); // Убедитесь, что сообщение совпадает

        // Проверяем, что findByUserIdAndId был вызван ровно один раз - для sourceCardId
        verify(bankCardRepository, times(1)).findByUserIdAndId(userId, sourceCardId);
        // Проверяем, что findByUserIdAndId НЕ был вызван для targetCardId
        verify(bankCardRepository, never()).findByUserIdAndId(userId, targetCardId);
        // Альтернатива: проверить, что всего было 1 вызов findByUserIdAndId (с любыми аргументами)
        // verify(bankCardRepository, times(1)).findByUserIdAndId(anyLong(), anyLong());
        // Или проверить, что save не вызывался вообще
        verify(bankCardRepository, never()).save(any(BankCards.class));
    }

    @Test
    void testTransferBetweenOwnCards_TargetCardNotFound() {
        Long userId = 1L;
        Long sourceCardId = 10L;
        Long targetCardId = 999L;
        BigDecimal amount = new BigDecimal("50.00");

        BankCards sourceCard = new BankCards();
        sourceCard.setId(sourceCardId);
        sourceCard.setUserId(userId);
        sourceCard.setStatus("ACTIVE");
        sourceCard.setBalance(new BigDecimal("100.00"));

        when(bankCardRepository.findByUserIdAndId(userId, sourceCardId)).thenReturn(Optional.of(sourceCard));
        when(bankCardRepository.findByUserIdAndId(userId, targetCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.transferBetweenOwnCards(userId, sourceCardId, targetCardId, amount))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Source card not found or access denied");

        verify(bankCardRepository, times(1)).findByUserIdAndId(userId, sourceCardId);
        verify(bankCardRepository, times(1)).findByUserIdAndId(userId, targetCardId);
        verify(bankCardRepository, never()).save(any(BankCards.class));
    }

    @Test
    void testTransferBetweenOwnCards_SourceCardNotActive() {
        Long userId = 1L;
        Long sourceCardId = 10L;
        Long targetCardId = 20L;
        BigDecimal amount = new BigDecimal("50.00");

        BankCards sourceCard = new BankCards();
        sourceCard.setId(sourceCardId);
        sourceCard.setUserId(userId);
        sourceCard.setStatus("BLOCKED"); // Не активна

        BankCards targetCard = new BankCards();
        targetCard.setId(targetCardId);
        targetCard.setUserId(userId);
        targetCard.setStatus("ACTIVE");

        when(bankCardRepository.findByUserIdAndId(userId, sourceCardId)).thenReturn(Optional.of(sourceCard));
        when(bankCardRepository.findByUserIdAndId(userId, targetCardId)).thenReturn(Optional.of(targetCard));

        assertThatThrownBy(() -> userService.transferBetweenOwnCards(userId, sourceCardId, targetCardId, amount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Source card is not active");

        verify(bankCardRepository, times(1)).findByUserIdAndId(userId, sourceCardId);
        verify(bankCardRepository, times(1)).findByUserIdAndId(userId, targetCardId);
        verify(bankCardRepository, never()).save(any(BankCards.class));
    }

    @Test
    void testTransferBetweenOwnCards_InsufficientFunds() {
        Long userId = 1L;
        Long sourceCardId = 10L;
        Long targetCardId = 20L;
        BigDecimal amount = new BigDecimal("150.00"); // Больше, чем на балансе

        BankCards sourceCard = new BankCards();
        sourceCard.setId(sourceCardId);
        sourceCard.setUserId(userId);
        sourceCard.setStatus("ACTIVE");
        sourceCard.setBalance(new BigDecimal("100.00")); // Меньше, чем amount

        BankCards targetCard = new BankCards();
        targetCard.setId(targetCardId);
        targetCard.setUserId(userId);
        targetCard.setStatus("ACTIVE");

        when(bankCardRepository.findByUserIdAndId(userId, sourceCardId)).thenReturn(Optional.of(sourceCard));
        when(bankCardRepository.findByUserIdAndId(userId, targetCardId)).thenReturn(Optional.of(targetCard));

        assertThatThrownBy(() -> userService.transferBetweenOwnCards(userId, sourceCardId, targetCardId, amount))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient funds on source card");

        verify(bankCardRepository, times(1)).findByUserIdAndId(userId, sourceCardId);
        verify(bankCardRepository, times(1)).findByUserIdAndId(userId, targetCardId);
        verify(bankCardRepository, never()).save(any(BankCards.class));
    }

    // --- Тесты для получения баланса ---

    @Test
    void testGetCardBalance_Success() {
        Long userId = 1L;
        Long cardId = 10L;
        BigDecimal balance = new BigDecimal("123.45");

        BankCards mockCard = new BankCards();
        mockCard.setId(cardId);
        mockCard.setUserId(userId);
        mockCard.setBalance(balance);

        when(bankCardRepository.findByUserIdAndId(userId, cardId)).thenReturn(Optional.of(mockCard));

        BigDecimal result = userService.getCardBalance(userId, cardId);

        assertThat(result).isEqualByComparingTo(balance);
        verify(bankCardRepository, times(1)).findByUserIdAndId(userId, cardId);
    }

    @Test
    void testGetCardBalance_NotFound() {
        Long userId = 1L;
        Long cardId = 999L;

        when(bankCardRepository.findByUserIdAndId(userId, cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCardBalance(userId, cardId))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Card not found or access denied");

        verify(bankCardRepository, times(1)).findByUserIdAndId(userId, cardId);
    }

    // --- Тесты для получения информации о пользователе ---

    @Test
    void testGetUserInfo_Success() {
        Long userId = 1L;
        Users mockUser = new Users("testuser", "password", "ROLE_USER");
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        Users result = userService.getUserInfo(userId);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetUserInfo_NotFound() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, times(1)).findById(userId);
    }

    // --- Тесты для получения общего баланса ---

    @Test
    void testGetTotalBalanceByUserId() {
        Long userId = 1L;
        BigDecimal totalBalance = new BigDecimal("300.00");

        when(bankCardRepository.sumBalanceByUserId(userId)).thenReturn(totalBalance);

        BigDecimal result = userService.getTotalBalanceByUserId(userId);

        assertThat(result).isEqualByComparingTo(totalBalance);
        verify(bankCardRepository, times(1)).sumBalanceByUserId(userId);
    }

    @Test
    void testGetTotalBalanceByUserId_NullResult() {
        Long userId = 1L;

        when(bankCardRepository.sumBalanceByUserId(userId)).thenReturn(null);

        BigDecimal result = userService.getTotalBalanceByUserId(userId);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        verify(bankCardRepository, times(1)).sumBalanceByUserId(userId);
    }
}