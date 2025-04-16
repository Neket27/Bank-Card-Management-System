package app.bankcardmanagementsystem.controller.dto.limitOnOperationByCard;

import app.bankcardmanagementsystem.entity.LimitCard;

import java.math.BigDecimal;

public record CreateLimitOnOperationByCard(
        LimitCard limitCard,
        BigDecimal amount
) {
}
