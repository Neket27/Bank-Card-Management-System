package app.bankcardmanagementsystem.controller.dto.card;

import app.bankcardmanagementsystem.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCardDto(
        String cardNumber,
        String cardHolder,
        LocalDate expiryDate,
        BigDecimal balance,
        CardStatus status
) {}
