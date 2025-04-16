package app.bankcardmanagementsystem.mapper;

import app.bankcardmanagementsystem.controller.dto.limitOnOperationByCard.CreateLimitOnOperationByCard;
import app.bankcardmanagementsystem.entity.LimitOnOperationByCard;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LimitOnOperationByCardMapper {

    LimitOnOperationByCard toEntity(CreateLimitOnOperationByCard createLimitOnOperationByCard);
}
