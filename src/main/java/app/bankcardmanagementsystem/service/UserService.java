package app.bankcardmanagementsystem.service;

import app.bankcardmanagementsystem.controller.dto.user.CreateUserDto;
import app.bankcardmanagementsystem.controller.dto.user.UpdateUserDto;
import app.bankcardmanagementsystem.controller.dto.user.UserDto;
import app.bankcardmanagementsystem.entity.Role;
import app.bankcardmanagementsystem.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Set;

public interface UserService {

    UserDetailsService getUserDetailsService();

    UserDto createUser(CreateUserDto createUserDto);

    UserDto updateDataUser(UpdateUserDto updateUserDto);

    List<UserDto> getListUsers();

    User getById(Long id);

    User getUserByEmail(String email);

    boolean remove(String email);

    boolean isUsernameAlreadyInUse(String email);

    boolean isEmailAlreadyInUse(String email);

    Set<Role> getListUserRole(String email);

}
