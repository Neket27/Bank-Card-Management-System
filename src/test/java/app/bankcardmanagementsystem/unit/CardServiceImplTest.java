package app.bankcardmanagementsystem.unit;

import app.bankcardmanagementsystem.entity.*;
import app.bankcardmanagementsystem.exception.NotFoundException;
import app.bankcardmanagementsystem.repository.CardRepository;
import app.bankcardmanagementsystem.repository.LimitOnOperationByCardRepository;
import app.bankcardmanagementsystem.repository.RequestsOnBlockCardRepository;
import app.bankcardmanagementsystem.service.UserService;
import app.bankcardmanagementsystem.service.impl.CardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class CardServiceImplTest {

    @InjectMocks
    private CardServiceImpl cardService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private RequestsOnBlockCardRepository requestsOnBlockCardRepository;

    @Mock
    private LimitOnOperationByCardRepository limitOnOperationByCardRepository;

    @Mock
    private UserService userService;

    @Test
    void createCard_success() {
        // Arrange
        Card card = new Card();
        when(cardRepository.save(card)).thenReturn(card);

        // Act
        Card result = cardService.createCard(card);

        // Assert
        assertThat(result).isNotNull();
        verify(cardRepository).save(card);
    }

    @Test
    void assignCardToUser_success() {
        // Arrange
        Card card = new Card();
        User user = new User();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);
        when(cardRepository.save(card)).thenReturn(card);

        // Act
        Card result = cardService.assignCardToUser("test@example.com", 1L);

        // Assert
        assertThat(result.getUser()).isEqualTo(user);
        verify(cardRepository).save(card);
    }

    @Test
    void assignCardToUser_cardNotFound() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> cardService.assignCardToUser("test@example.com", 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Card not found");
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@example.com")
    void blockCard_success() {
        // Arrange
        Card card = new Card();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        // Act
        Card result = cardService.blockCard(1L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(CardStatus.BLOCKED);
        verify(cardRepository).save(card);
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@example.com")
    void activateCard_success() {
        // Arrange
        Card card = new Card();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        // Act
        Card result = cardService.activateCard(1L);

        // Assert
        assertThat(result.getStatus()).isEqualTo(CardStatus.ACTIVE);
        verify(cardRepository).save(card);
    }

    @Test
    void deleteCard_success() {
        // Act
        cardService.deleteCard(1L);

        // Assert
        verify(cardRepository).deleteById(1L);
    }

    @Test
    void getAll_success() {
        // Arrange
        List<Card> cards = List.of(new Card(), new Card());
        when(cardRepository.findAll()).thenReturn(cards);

        // Act
        List<Card> result = cardService.getAll();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getAllCardsUser_success() {
        // Arrange
        List<Card> cards = List.of(new Card());
        when(cardRepository.findAllByUser_Email("test@example.com")).thenReturn(cards);

        // Act
        List<Card> result = cardService.getAllCardsUser("test@example.com");

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@example.com")
    void getCard_asAdmin_success() {
        // Arrange
        Card card = new Card();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        // Act
        Card result = cardService.getCard(1L);

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    @WithMockUser(roles = "USER", username = "user@example.com")
    void getCard_asUser_Owned_success() {
        // Arrange
        Card card = new Card();
        card.setId(1L);

        User user = new User();
        user.setCards(Set.of(card));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(userService.getUserByEmail("user@example.com")).thenReturn(user);

        // Act
        Card result = cardService.getCard(1L);

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    @WithMockUser(roles = "USER", username = "user@example.com")
    void getCard_asUser_NotOwned_fail() {
        // Arrange
        Card card = new Card();
        card.setId(1L);

        User user = new User();
        when(userService.getUserByEmail("user@example.com")).thenReturn(user);

        // Act + Assert
        assertThatThrownBy(() -> cardService.getCard(1L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Card for current user not found");
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@example.com")
    void setLimitOnOperationByCard_success() {
        // Arrange
        Card card = new Card();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        LimitOnOperationByCard limit = new LimitOnOperationByCard();
        limit.setAmount(new BigDecimal("500"));

        // Act
        cardService.setLimitOnOperationByCard(1L, limit);

        // Assert
        assertThat(limit.getRemainingCrdLimit()).isEqualTo(new BigDecimal("500"));
        assertThat(limit.getStartPeriud()).isBefore(limit.getEndPeriud());
        verify(limitOnOperationByCardRepository).save(limit);
    }

    @Test
    void requestOnBlockCard_success() {
        // Act
        cardService.requestOnBlockCard(1L, "please block");

        // Assert
        verify(requestsOnBlockCardRepository).save(any(RequestOnBlockCard.class));
    }

    @Test
    void encryptedNumberAndMasked_success() {
        // Arrange
        Card card = new Card();
        String cardNumber = "1234567812345678";

        // Act
        Card result = cardService.encryptedNumberAndMasked(card, cardNumber);

        // Assert
        assertThat(result.getEncryptedCardNumber()).isNotBlank();
        assertThat(result.getMaskedCardNumber()).startsWith("****");
    }
}
