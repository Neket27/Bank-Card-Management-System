package app.bankcardmanagementsystem.mapper;


import app.bankcardmanagementsystem.controller.dto.TransactionDto;
import app.bankcardmanagementsystem.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper extends BaseMapper<Transaction, TransactionDto> {
}
