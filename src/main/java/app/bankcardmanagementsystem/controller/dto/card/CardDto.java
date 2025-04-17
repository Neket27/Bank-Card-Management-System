package app.bankcardmanagementsystem.controller.dto.card;

import app.bankcardmanagementsystem.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

public record CardDto(
        @NotNull(message = "Id не может быть пустым")
        @Positive(message = "Id должен быть положительным")
        Long id,

        @NotBlank(message = "Encrypted card number не может быть пустым")
        @Size(max = 255, message = "Encrypted card number слишком длинный")
        String encryptedCardNumber,

        @NotBlank(message = "Card holder не может быть пустым")
        @Size(max = 255, message = "Card holder слишком длинный")
        String cardHolder,

        @NotNull(message = "Expiry date не может быть пустым")
        @Future(message = "Expiry date должна быть в будущем")
        LocalDate expiryDate,

        @NotNull(message = "Balance не может быть пустым")
        @DecimalMin(value = "0.00", inclusive = true, message = "Balance должен быть неотрицательным")
        BigDecimal balance,

        @NotNull(message = "Status не может быть пустым")
        CardStatus status,

        @NotNull(message = "UserId не может быть пустым")
        @Positive(message = "UserId должен быть положительным")
        Long userId
) {
}
