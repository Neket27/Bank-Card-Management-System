package app.bankcardmanagementsystem.service.impl;

import app.bankcardmanagementsystem.controller.dto.user.CreateUserDto;
import app.bankcardmanagementsystem.controller.dto.user.UpdateUserDto;
import app.bankcardmanagementsystem.controller.dto.user.UserDto;
import app.bankcardmanagementsystem.entity.Role;
import app.bankcardmanagementsystem.entity.User;
import app.bankcardmanagementsystem.exception.CreateException;
import app.bankcardmanagementsystem.exception.DeleteException;
import app.bankcardmanagementsystem.exception.NotFoundException;
import app.bankcardmanagementsystem.mapper.UserMapper;
import app.bankcardmanagementsystem.repository.UserRepo;
import app.bankcardmanagementsystem.service.UserService;
import app.bankcardmanagementsystem.utils.jwtToken.EncoderPassword;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Setter
@Getter
@Builder
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final JWTServiceImpl jwtService;

    @Override
    @Transactional
    public UserDetailsService getUserDetailsService() {
        return email -> userRepo.findByEmail(email).orElse(null);
    }

    @Override
    @Transactional
    public UserDto createUser(CreateUserDto createUserDto) {
        if (isUsernameAlreadyInUse(createUserDto.email()))
            throw new CreateException("Пользователь с логином " + createUserDto.email() + " уже существует");

        User user = User.builder()
                .password(EncoderPassword.encode(createUserDto.password()))
                .email(createUserDto.email())
                .roles(createUserDto.roles())
                .build();

        return userMapper.toDto(userRepo.save(user));
    }

    @Override
    @Transactional
    public UserDto updateDataUser(UpdateUserDto updateUserDto) {
        User user = getUserByEmail(updateUserDto.email());

        user.setRoles(updateUserDto.roles());

        if (!EncoderPassword.equalsPasswords(updateUserDto.password(), user.getPassword()))
            user.setPassword(EncoderPassword.encode(updateUserDto.password()));

        userRepo.save(user);

        return userMapper.toDto(userRepo.save(user));
    }

    @Override
    @Transactional
    public List<UserDto> getListUsers() {
        List<User> users = userRepo.findAll();
        return users.stream().map(user -> {
            user.setPassword(EncoderPassword.encode(user.getPassword()));
            return userMapper.toDto(user);
        }).toList();
    }

    @Override
    @Transactional
    public User getById(@NonNull Long id) {
        return userRepo.findById(id).orElseThrow(() -> new NotFoundException("Пользователь с id= " + id + " не найден"));
    }

    @Override
    @Transactional
    public User getUserByEmail(@NonNull String email) {
        try {
            return userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("Пользователь с логином " + email + " не найден"));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    public boolean remove(@NonNull String username) {
        try {
            Long id = getUserByEmail(username).getId();
            userRepo.deleteById(id);
            return true;
        } catch (DeleteException e) {
            log.error("Ошибка удаления пользователя: ", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean isUsernameAlreadyInUse(@NonNull String email) {
        return userRepo.existsUserByEmail(email);
    }

    @Override
    @Transactional
    public boolean isEmailAlreadyInUse(@NonNull String email) {
        return userRepo.existsUserByEmail(email);
    }

    @Override
    @Transactional
    public Set<Role> getListUserRole(String username) {
        return userRepo.findByEmail(username).orElseThrow(() -> new NotFoundException("Пользоватьль не найден")).getRoles();
    }

}