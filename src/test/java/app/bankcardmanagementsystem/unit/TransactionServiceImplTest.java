package app.bankcardmanagementsystem.unit;

import app.bankcardmanagementsystem.controller.dto.TransactionDto;
import app.bankcardmanagementsystem.controller.dto.TransferRequestDto;
import app.bankcardmanagementsystem.entity.*;
import app.bankcardmanagementsystem.exception.CreateException;
import app.bankcardmanagementsystem.repository.CardRepository;
import app.bankcardmanagementsystem.repository.LimitOnOperationByCardRepository;
import app.bankcardmanagementsystem.repository.TransactionRepository;
import app.bankcardmanagementsystem.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LimitOnOperationByCardRepository limitOnOperationByCardRepository;

    @Mock
    private CardRepository cardRepository;

    @Test
    void addTransaction_success() {
        // Arrange
        Long cardId = 1L;
        BigDecimal cardBalance = new BigDecimal("1000");
        BigDecimal transactionAmount = new BigDecimal("100");

        Card card = Card.builder()
                .id(cardId)
                .balance(cardBalance)
                .build();

        TransactionDto dto = TransactionDto.builder()
                .amount(transactionAmount)
                .description("Test")
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(limitOnOperationByCardRepository.findByCard_Id(cardId)).thenReturn(List.of());
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Transaction result = transactionService.addTransaction(cardId, dto);

        // Assert
        assertThat(result.getAmount()).isEqualTo(transactionAmount);
        assertThat(result.getDescription()).isEqualTo("Test");
        verify(cardRepository, times(1)).save(card);
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void addTransaction_insufficientFunds_throwsCreateException() {
        // Arrange
        Long cardId = 1L;
        BigDecimal cardBalance = new BigDecimal("50");
        BigDecimal transactionAmount = new BigDecimal("100");

        Card card = Card.builder()
                .id(cardId)
                .balance(cardBalance)
                .build();

        TransactionDto dto = TransactionDto.builder()
                .description("Test")
                .amount(transactionAmount)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        // Act & Assert
        assertThatThrownBy(() -> transactionService.addTransaction(cardId, dto))
                .isInstanceOf(CreateException.class)
                .hasMessageContaining("Недостаточно средств");
    }

    @Test
    void addTransaction_exceedsDayLimit_throwsCreateException() {
        // Arrange
        Long cardId = 1L;
        BigDecimal cardBalance = new BigDecimal("1000");
        BigDecimal transactionAmount = new BigDecimal("300");

        Card card = Card.builder()
                .id(cardId)
                .balance(cardBalance)
                .build();

        TransactionDto dto = TransactionDto.builder()
                .amount(transactionAmount)
                .description("Test")
                .build();

        LimitOnOperationByCard limit = LimitOnOperationByCard.builder()
                .remainingCrdLimit(new BigDecimal("200"))
                .limitCard(LimitCard.DAY)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(limitOnOperationByCardRepository.findByCard_Id(cardId)).thenReturn(List.of(limit));

        // Act & Assert
        assertThatThrownBy(() -> transactionService.addTransaction(cardId, dto))
                .isInstanceOf(CreateException.class)
                .hasMessageContaining("дневного бюджета");
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@example.com")
    void getCardTransactions_successForAdmin() {
        // Arrange
        Long cardId = 1L;

        Card card = Card.builder()
                .id(cardId)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(transactionRepository.findByCardId(cardId)).thenReturn(List.of());

        // Act
        List<Transaction> result = transactionService.getCardTransactions(cardId);

        // Assert
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @WithMockUser(roles = "USER", username = "user@example.com")
    void getCardTransactions_accessDeniedForNonOwner() {
        // Arrange
        Long cardId = 1L;

        User otherUser = User.builder()
                .email("other@example.com")
                .build();

        Card card = Card.builder()
                .id(cardId)
                .user(otherUser)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        // Act & Assert
        assertThatThrownBy(() -> transactionService.getCardTransactions(cardId))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    @WithMockUser(roles = "USER", username = "user@example.com")
    void transferBetweenOwnCards_success() {
        // Arrange
        Long fromId = 1L;
        Long toId = 2L;

        User user = User.builder()
                .email("user@example.com")
                .build();

        Card fromCard = Card.builder()
                .id(fromId)
                .balance(new BigDecimal("500"))
                .user(user)
                .build();

        Card toCard = Card.builder()
                .id(toId)
                .balance(new BigDecimal("100"))
                .user(user)
                .build();

        TransferRequestDto dto = new TransferRequestDto(fromId, toId, new BigDecimal("200"), "Transfer");

        when(cardRepository.findById(fromId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toId)).thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Transaction result = transactionService.transferBetweenOwnCards(dto);

        // Assert
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("-200"));
        assertThat(fromCard.getBalance()).isEqualTo(new BigDecimal("300"));
        assertThat(toCard.getBalance()).isEqualTo(new BigDecimal("300"));
    }

    @Test
    @WithMockUser(roles = "USER", username = "user@example.com")
    void transferBetweenOwnCards_insufficientFunds_throwsException() {
        // Arrange
        Long fromId = 1L;
        Long toId = 2L;

        User user = User.builder()
                .email("user@example.com")
                .build();

        Card fromCard = Card.builder()
                .id(fromId)
                .balance(new BigDecimal("100"))
                .user(user)
                .build();

        Card toCard = Card.builder()
                .id(toId)
                .balance(new BigDecimal("100"))
                .user(user)
                .build();

        TransferRequestDto dto = new TransferRequestDto(fromId, toId, new BigDecimal("200"), "Transfer");

        when(cardRepository.findById(fromId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toId)).thenReturn(Optional.of(toCard));

        // Act & Assert
        assertThatThrownBy(() -> transactionService.transferBetweenOwnCards(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    @WithMockUser(roles = "USER", username = "user@example.com")
    void transferBetweenOwnCards_notOwnCard_throwsCreateException() {
        // Arrange
        Long fromId = 1L;
        Long toId = 2L;

        User otherUser = User.builder()
                .email("someone@example.com")
                .build();

        User currentUser = User.builder()
                .email("user@example.com")
                .build();

        Card fromCard = Card.builder()
                .id(fromId)
                .user(otherUser)
                .build();

        Card toCard = Card.builder()
                .id(toId)
                .user(currentUser)
                .build();

        TransferRequestDto dto = new TransferRequestDto(fromId, toId, new BigDecimal("50"), "Transfer");

        when(cardRepository.findById(fromId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toId)).thenReturn(Optional.of(toCard));

        // Act & Assert
        assertThatThrownBy(() -> transactionService.transferBetweenOwnCards(dto))
                .isInstanceOf(CreateException.class)
                .hasMessageContaining("Access denied");
    }
}
