package app.bankcardmanagementsystem.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class TransactionDto {
    private String description;
    private BigDecimal amount;
}