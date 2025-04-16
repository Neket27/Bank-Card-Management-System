package app.bankcardmanagementsystem.mapper;

import app.bankcardmanagementsystem.controller.dto.card.CardDto;
import app.bankcardmanagementsystem.controller.dto.card.CreateCardDto;
import app.bankcardmanagementsystem.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper extends BaseMapper<Card, CardDto> {

    Card toEntity(CreateCardDto cardDto);

    @Mapping(target = "userId", source = "user.id")
    CardDto toDto(Card card);
}
