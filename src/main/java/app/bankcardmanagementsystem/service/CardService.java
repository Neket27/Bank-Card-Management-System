package app.bankcardmanagementsystem.service;

import app.bankcardmanagementsystem.entity.Card;
import app.bankcardmanagementsystem.entity.LimitOnOperationByCard;

import java.util.List;

public interface CardService {
    Card createCard(Card card);

    Card assignCardToUser(String login, Long cardId);

    Card blockCard(Long id);

    Card activateCard(Long id);

    void deleteCard(Long id);

    List<Card> getAll();

    List<Card> getAllCardsUser(String email);

    Card getCard(Long id);

    void setLimitOnOperationByCard(Long idCard, LimitOnOperationByCard limitOnOperationByCard);

    void requestOnBlockCard(Long id, String massage);

    Card encryptedNumberAndMasked(Card card, String cardNumber);
}
