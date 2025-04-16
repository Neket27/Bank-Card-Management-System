package app.bankcardmanagementsystem.service.impl;

import app.bankcardmanagementsystem.entity.*;
import app.bankcardmanagementsystem.exception.NotFoundException;
import app.bankcardmanagementsystem.repository.CardRepository;
import app.bankcardmanagementsystem.repository.LimitOnOperationByCardRepository;
import app.bankcardmanagementsystem.repository.RequestsOnBlockCardRepository;
import app.bankcardmanagementsystem.service.CardService;
import app.bankcardmanagementsystem.service.UserService;
import app.bankcardmanagementsystem.utils.CardEncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final RequestsOnBlockCardRepository requestsOnBlockCardRepository;
    private final LimitOnOperationByCardRepository limitOnOperationByCardRepository;
    private final UserService userService;

    @Override
    public Card createCard(Card card) {
        return cardRepository.save(card);
    }

    @Override
    public Card assignCardToUser(String login, Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new NotFoundException("Card not found"));
        User userByEmail = userService.getUserByEmail(login);
        card.setUser(userByEmail);
        return cardRepository.save(card);
    }

    @Override
    public Card blockCard(Long id) {
        Card card = getCard(id);
        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    @Override
    public Card activateCard(Long id) {
        Card card = getCard(id);
        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    @Override
    public void deleteCard(Long id) {
        cardRepository.deleteById(id);
    }

    @Override
    public List<Card> getAll() {
        return cardRepository.findAll();
    }

    @Override
    public List<Card> getAllCardsUser(String email) {
        return cardRepository.findAllByUser_Email(email);
    }

    @Override
    public Card getCard(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            User userByEmail = userService.getUserByEmail(authentication.getName());
            if (userByEmail.getCards().stream().anyMatch(card -> card.getId().equals(id)))
                return cardRepository.findById(id).orElseThrow(() -> new NotFoundException("Card not found"));
            else throw new SecurityException("Card for current user not found");
        }

        return cardRepository.findById(id).orElseThrow(() -> new NotFoundException("Card not found"));
    }

    @Override
    public void setLimitOnOperationByCard(Long idCard, LimitOnOperationByCard limitOnOperationByCard) {
        limitOnOperationByCard.setCard(getCard(idCard));
        limitOnOperationByCard.setRemainingCrdLimit(limitOnOperationByCard.getAmount());
        LocalDateTime startTime = LocalDateTime.now();
        limitOnOperationByCard
                .setStartPeriud(startTime);
        limitOnOperationByCard.setEndPeriud(startTime.plusMonths(1));
        limitOnOperationByCardRepository.save(limitOnOperationByCard);
    }

    @Override
    public void requestOnBlockCard(Long id, String massage) {
        requestsOnBlockCardRepository.save(new RequestOnBlockCard(id, massage));
    }

    @Override
    public Card encryptedNumberAndMasked(Card card, String cardNumber) {
        String encrypted = CardEncryptionUtil.encrypt(cardNumber);
        String masked = CardEncryptionUtil.maskCard(cardNumber);
        card.setEncryptedCardNumber(encrypted);
        card.setMaskedCardNumber(masked);
        return card;
    }

}
