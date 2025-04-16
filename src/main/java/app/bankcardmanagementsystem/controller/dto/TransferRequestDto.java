package app.bankcardmanagementsystem.controller.dto;

import java.math.BigDecimal;

public record TransferRequestDto(
        Long fromCardId,
        Long toCardId,
        BigDecimal amount,
        String description
) {
}
