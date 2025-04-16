package app.bankcardmanagementsystem.controller.dto.card;

import app.bankcardmanagementsystem.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardDto(
        Long id,
        String encryptedCardNumber,
        String cardHolder,
        LocalDate expiryDate,
        BigDecimal balance,
        CardStatus status,
        Long userId
) {
}