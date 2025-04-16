package app.bankcardmanagementsystem.mapper;


import app.bankcardmanagementsystem.controller.dto.jwtToken.SignUpRequest;
import app.bankcardmanagementsystem.controller.dto.user.CreateUserDto;
import app.bankcardmanagementsystem.controller.dto.user.UserDto;
import app.bankcardmanagementsystem.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    CreateUserDto toCreateUserDto(SignUpRequest signUpRequest);

    User toEntity(UserDto userDto);

    UserDto toDto(User user);
}
