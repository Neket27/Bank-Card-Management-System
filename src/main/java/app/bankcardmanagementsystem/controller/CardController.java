package app.bankcardmanagementsystem.controller;

import app.bankcardmanagementsystem.controller.advice.annotation.CustomExceptionHandler;
import app.bankcardmanagementsystem.controller.dto.TransactionDto;
import app.bankcardmanagementsystem.controller.dto.TransferRequestDto;
import app.bankcardmanagementsystem.controller.dto.card.CardDto;
import app.bankcardmanagementsystem.controller.dto.card.CreateCardDto;
import app.bankcardmanagementsystem.controller.dto.limitOnOperationByCard.CreateLimitOnOperationByCard;
import app.bankcardmanagementsystem.entity.Card;
import app.bankcardmanagementsystem.entity.LimitOnOperationByCard;
import app.bankcardmanagementsystem.entity.Transaction;
import app.bankcardmanagementsystem.mapper.CardMapper;
import app.bankcardmanagementsystem.mapper.LimitOnOperationByCardMapper;
import app.bankcardmanagementsystem.mapper.TransactionMapper;
import app.bankcardmanagementsystem.service.impl.CardServiceImpl;
import app.bankcardmanagementsystem.service.impl.TransactionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Card Management", description = "Управление банковскими картами и транзакциями")
@CustomExceptionHandler
public class CardController {

    private final CardServiceImpl cardService;
    private final CardMapper cardMapper;
    private final TransactionServiceImpl transactionService;
    private final TransactionMapper transactionMapper;
    private final LimitOnOperationByCardMapper limitOnOperationByCardMapper;

    @Operation(
            summary = "Создание новой карты",
            description = "Доступно только для пользователей с ролью ADMIN",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public CardDto create(@RequestBody CreateCardDto dto) {
        Card entity = cardMapper.toEntity(dto);
        entity = cardService.encryptedNumberAndMasked(entity, dto.cardNumber());
        return cardMapper.toDto(cardService.createCard(entity));
    }


    @Operation(
            summary = "Назначить карту пользователю",
            description = "Доступно только для пользователей с ролью ADMIN",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована",
            content = @Content(schema = @Schema(implementation = CardDto.class)))
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}/assign/{email}")
    public CardDto assignCardToUser(@PathVariable String email, @PathVariable Long id) {
        return cardMapper.toDto(cardService.assignCardToUser(email, id));
    }

    @Operation(
            summary = "Заблокировать карту по ID",
            description = "Доступно только для пользователей с ролью ADMIN",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована",
            content = @Content(schema = @Schema(implementation = CardDto.class)))
    @PutMapping("/{id}/block")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public CardDto block(@PathVariable Long id) {
        Card card = cardService.blockCard(id);
        return cardMapper.toDto(card);
    }

    @Operation(
            summary = "Активировать карту по ID",
            description = "Доступно только для пользователей с ролью ADMIN",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Карта успешно активирована",
            content = @Content(schema = @Schema(implementation = CardDto.class)))
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public CardDto activate(@PathVariable Long id) {
        Card card = cardService.activateCard(id);
        return cardMapper.toDto(card);
    }

    @Operation(
            summary = "Удалить карту по ID",
            description = "Доступно только для пользователей с ролью ADMIN",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public void delete(@PathVariable Long id) {
        cardService.deleteCard(id);
    }

    @Operation(
            summary = "Получить все карты",
            description = "Доступно только для пользователей с ролью ADMIN",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Список карт",
            content = @Content(schema = @Schema(implementation = Card.class)))
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public List<CardDto> getAll() {
        return cardMapper.toDtoList(cardService.getAll());
    }

    @Operation(
            summary = "Получить карту по ID",
            description = "Доступно для пользователей с ролью USER",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта найдена",
                    content = @Content(schema = @Schema(implementation = Card.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public CardDto getById(@PathVariable Long id) {
        return cardMapper.toDto(cardService.getCard(id));
    }

    @Operation(
            summary = "Установка лимита на карту",
            description = "Доступно только для пользователей с ролью ADMIN",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Лимит установлен")
    @PostMapping("/{id}/limit")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public void setLimitOnOperationByCard(@PathVariable Long id, @RequestBody CreateLimitOnOperationByCard limitOnOperationByCard) {
        LimitOnOperationByCard limit = limitOnOperationByCardMapper.toEntity(limitOnOperationByCard);
        cardService.setLimitOnOperationByCard(id, limit);
    }

    @Operation(
            summary = "Добавить транзакцию к карте",
            description = "Доступно для пользователей с ролью USER",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Транзакция добавлена",
            content = @Content(schema = @Schema(implementation = TransactionDto.class)))
    @PostMapping("/{id}/transactions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public TransactionDto addTransaction(@PathVariable Long id, @RequestBody TransactionDto dto) {
        Transaction transaction = transactionService.addTransaction(id, dto);
        return transactionMapper.toDto(transaction);
    }

    @Operation(
            summary = "Получить транзакции карты по ID",
            description = "Доступно только для пользователей с ролью USER",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "История транзакций",
            content = @Content(schema = @Schema(implementation = TransactionDto.class)))
    @GetMapping("/{id}/transactions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public List<TransactionDto> getTransactions(@PathVariable Long id) {
        List<Transaction> cardTransactions = transactionService.getCardTransactions(id);
        return transactionMapper.toDtoList(cardTransactions);
    }

    ////////////////////////////////////////////////////////////////////

    @Operation(
            summary = "Получить все карты текущего пользователя",
            description = "Доступно для пользователей с ролью USER",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Список карт текущего пользователя",
            content = @Content(schema = @Schema(implementation = Card.class)))
    @GetMapping("/user")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public List<CardDto> getAllCardCurrentUser() {
        String username = getEmailCurrentUser();
        List<Card> allCardsUser = cardService.getAllCardsUser(username);
        return cardMapper.toDtoList(allCardsUser);
    }

    @Operation(
            summary = "Запрос на блокировку карты текущего пользователя",
            description = "Доступно для пользователей с ролью USER",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{id}/request/block")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public void requestOnBlockCard(@PathVariable Long id, @RequestParam(defaultValue = "Пользователь просит заблокировать карту с id") String massage) {
        cardService.requestOnBlockCard(id, massage + " :" + id);
    }


    @Operation(
            summary = "Перевод между своих карт",
            description = "Доступно для пользователей с ролью USER",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Перевод между своих карт выполнен успешно")
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public TransactionDto transfer(@RequestBody TransferRequestDto dto) {
        return transactionMapper.toDto(transactionService.transferBetweenOwnCards(dto));
    }

    private String getEmailCurrentUser() {
        return (String) RequestContextHolder.currentRequestAttributes().getAttribute("login", RequestAttributes.SCOPE_REQUEST);
    }

}
