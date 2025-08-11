package com.example.bankcards.service;

import com.example.bankcards.entity.BankCards;
import com.example.bankcards.repository.BankCardRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
//Здесь довольно мало методов, т.к либо методы для карт подходили только ADMIN, например установка статуса карт ACTIVE, либо только USER. Если нужно будет
//доработать проект, в будущем можно и нужно будет перенести все методы для карт сюда
@Service
public class BankCardService {

    private final BankCardRepository bankCardRepository;

    public BankCardService(BankCardRepository bankCardRepository) {
        this.bankCardRepository = bankCardRepository;
    }

    /**
     * Получение карты по ID
     */
    public Optional<BankCards> getCardById(Long id) {
        return bankCardRepository.findById(id);
    }

    /**
     * Получение всех карт
     */
    public List<BankCards> getAllCards() {
        return bankCardRepository.findAll();
    }

}