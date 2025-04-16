package app.bankcardmanagementsystem.repository;

import app.bankcardmanagementsystem.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCardId(Long cardId);
}
