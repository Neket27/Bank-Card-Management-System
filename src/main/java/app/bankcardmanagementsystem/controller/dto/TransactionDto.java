package app.bankcardmanagementsystem.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionDto {
    private String description;
    private BigDecimal amount;
}