package com.example.bankcards.repository;

import com.example.bankcards.entity.BankCards;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankCardRepository extends JpaRepository<BankCards, Long> {
    List<BankCards> findByUserId(Long userId);

    Page<BankCards> findByUserId(Long userId, Pageable pageable);

    List<BankCards> findByUserIdAndStatus(Long userId, String status);

    Optional<BankCards> findByCardNumber(String cardNumber);

    boolean existsByCardNumber(String cardNumber);


    @Query("SELECT SUM(b.balance) FROM BankCards b WHERE b.user.id = :userId")
    BigDecimal sumBalanceByUserId(@Param("userId") Long userId);


    // Находит активные карты с истекшим сроком
    @Query("SELECT c FROM BankCards c WHERE c.status = 'ACTIVE' AND c.expiryDate < :currentDate")
    List<BankCards> findActiveExpiredCards(@Param("currentDate") LocalDate currentDate);

    Optional<BankCards> findByUserIdAndId(Long userId, Long id);

}
