package app.bankcardmanagementsystem.service.impl;


import app.bankcardmanagementsystem.controller.dto.TransactionDto;
import app.bankcardmanagementsystem.controller.dto.TransferRequestDto;
import app.bankcardmanagementsystem.entity.Card;
import app.bankcardmanagementsystem.entity.LimitCard;
import app.bankcardmanagementsystem.entity.LimitOnOperationByCard;
import app.bankcardmanagementsystem.entity.Transaction;
import app.bankcardmanagementsystem.exception.CreateException;
import app.bankcardmanagementsystem.exception.NotFoundException;
import app.bankcardmanagementsystem.repository.CardRepository;
import app.bankcardmanagementsystem.repository.LimitOnOperationByCardRepository;
import app.bankcardmanagementsystem.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl {

    private final TransactionRepository transactionRepository;
    private final LimitOnOperationByCardRepository limitOnOperationByCardRepository;
    private final CardRepository cardRepository;

    @Transactional
    public Transaction addTransaction(Long cardId, TransactionDto dto) {
        Card card = cardRepository.findById(cardId).orElseThrow();
        BigDecimal amountAfterPurchase = card.getBalance().subtract(dto.getAmount());
        if (amountAfterPurchase.compareTo(BigDecimal.ZERO) < 0)
            throw new CreateException("Недостаточно средств на балансе, необходимо пополнить баланс на сумму: " + amountAfterPurchase.abs());

        List<LimitOnOperationByCard> limitOnOperationByCards = limitOnOperationByCardRepository.findByCard_Id(cardId);

        limitOnOperationByCards.forEach(limitOnOperationByCard -> {

            BigDecimal balanceAfterTransaction = limitOnOperationByCard.getRemainingCrdLimit().subtract(dto.getAmount());
            if (limitOnOperationByCard.getLimitCard().equals(LimitCard.DAY) && balanceAfterTransaction.compareTo(BigDecimal.ZERO) < 0) {
                throw new CreateException("Выход за рамки дневного бюджета, транзакция отменена");
            } else {
                limitOnOperationByCard.setRemainingCrdLimit(limitOnOperationByCard.getRemainingCrdLimit().subtract(dto.getAmount()));
                limitOnOperationByCardRepository.save(limitOnOperationByCard);
            }

            if (limitOnOperationByCard.getLimitCard().equals(LimitCard.MONTH) && balanceAfterTransaction.compareTo(BigDecimal.ZERO) < 0) {
                throw new CreateException("Выход за рамки месячного бюджета, транзакция отменена");
            } else {
                limitOnOperationByCard.setRemainingCrdLimit(limitOnOperationByCard.getRemainingCrdLimit().subtract(dto.getAmount()));
                limitOnOperationByCardRepository.save(limitOnOperationByCard);
            }
        });

        Transaction tx = Transaction.builder()
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .timestamp(LocalDateTime.now())
                .card(card)
                .build();

        card.setBalance(amountAfterPurchase);
        cardRepository.save(card);

        return transactionRepository.save(tx);
    }

    @Transactional
    public List<Transaction> getCardTransactions(Long cardId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Card card = cardRepository.findById(cardId).orElseThrow();

        String s= auth.getName();
        if (!isAdmin && !card.getUser().getEmail().equals(s)) {
            throw new SecurityException("Access denied: not your card");
        }
        return transactionRepository.findByCardId(cardId);
    }

    public Transaction transferBetweenOwnCards(TransferRequestDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();

        Card fromCard = cardRepository.findById(dto.fromCardId()).orElseThrow(()-> new NotFoundException("Card not found with id: " + dto.fromCardId()));
        Card toCard = cardRepository.findById(dto.toCardId()).orElseThrow(()-> new NotFoundException("Card not found with id: " + dto.toCardId()));

        if (!fromCard.getUser().getEmail().equals(currentUser) || !toCard.getUser().getEmail().equals(currentUser)) {
            throw new CreateException("Access denied: can only transfer between your own cards.");
        }

        if (fromCard.getBalance().compareTo(dto.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds.");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(dto.amount()));
        toCard.setBalance(toCard.getBalance().add(dto.amount()));

        Transaction transaction = Transaction.builder()
                .description(dto.description() != null ? dto.description() : "Transfer to card " + toCard.getMaskedCardNumber())
                .amount(dto.amount().negate())
                .timestamp(LocalDateTime.now())
                .card(fromCard)
                .build();

        Transaction income = Transaction.builder()
                .description("Incoming transfer from card " + fromCard.getMaskedCardNumber())
                .amount(dto.amount())
                .timestamp(LocalDateTime.now())
                .card(toCard)
                .build();

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
        transactionRepository.save(income);
        return transactionRepository.save(transaction);
    }

}
