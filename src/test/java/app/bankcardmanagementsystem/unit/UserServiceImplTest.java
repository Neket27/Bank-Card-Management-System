package app.bankcardmanagementsystem.unit;

import app.bankcardmanagementsystem.controller.dto.user.CreateUserDto;
import app.bankcardmanagementsystem.controller.dto.user.UpdateUserDto;
import app.bankcardmanagementsystem.controller.dto.user.UserDto;
import app.bankcardmanagementsystem.entity.Role;
import app.bankcardmanagementsystem.entity.User;
import app.bankcardmanagementsystem.exception.CreateException;
import app.bankcardmanagementsystem.exception.NotFoundException;
import app.bankcardmanagementsystem.mapper.UserMapper;
import app.bankcardmanagementsystem.repository.UserRepo;
import app.bankcardmanagementsystem.service.impl.JWTServiceImpl;
import app.bankcardmanagementsystem.service.impl.UserServiceImpl;
import app.bankcardmanagementsystem.utils.jwtToken.EncoderPassword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepo userRepo;
    private UserMapper userMapper;
    private JWTServiceImpl jwtService;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepo.class);
        userMapper = mock(UserMapper.class);
        jwtService = mock(JWTServiceImpl.class);
        userService = new UserServiceImpl(userRepo, userMapper, jwtService);
    }

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        // Arrange
        CreateUserDto createUserDto = new CreateUserDto("test@example.com", "password", Set.of(Role.ROLE_USER));
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .roles(Set.of(Role.ROLE_USER))
                .build();

        UserDto userDto = new UserDto(1L, "test@example.com", Set.of(Role.ROLE_USER));

        when(userRepo.existsUserByEmail(createUserDto.email())).thenReturn(false);
        when(userRepo.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act
        UserDto result = userService.createUser(createUserDto);

        // Assert
        assertThat(result).isEqualTo(userDto);
        verify(userRepo).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowExceptionIfUserAlreadyExists() {
        // Arrange
        CreateUserDto createUserDto = new CreateUserDto("test@example.com", "password", Set.of(Role.ROLE_USER));

        when(userRepo.existsUserByEmail(createUserDto.email())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(createUserDto))
                .isInstanceOf(CreateException.class)
                .hasMessageContaining("Пользователь с логином password уже существует");

        verify(userRepo, never()).save(any());
    }

    @Test
    void updateDataUser_shouldUpdateUser() {
        // Arrange
        UpdateUserDto updateUserDto = new UpdateUserDto("test@example.com", "newPassword", Set.of(Role.ROLE_ADMIN));
        User existingUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password(EncoderPassword.encode("oldPassword"))
                .roles(Set.of(Role.ROLE_USER))
                .build();

        UserDto updatedUserDto = new UserDto(1L, "test@example.com", Set.of(Role.ROLE_ADMIN));

        when(userRepo.findByEmail(updateUserDto.email())).thenReturn(Optional.of(existingUser));
        when(userRepo.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toDto(any(User.class))).thenReturn(updatedUserDto);

        // Act
        UserDto result = userService.updateDataUser(updateUserDto);

        // Assert
        assertThat(result).isEqualTo(updatedUserDto);
        verify(userRepo, times(2)).save(existingUser); // У тебя 2 вызова save() в методе
    }

    @Test
    void getListUsers_shouldReturnEncodedUsersList() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .password("password")
                .roles(Set.of(Role.ROLE_USER))
                .build();


        UserDto userDto = new UserDto(1L, "user@example.com", Set.of(Role.ROLE_USER));

        when(userRepo.findAll()).thenReturn(List.of(user));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act
        List<UserDto> result = userService.getListUsers();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("user@example.com");
    }

    @Test
    void getById_shouldReturnUserIfExists() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .roles(Set.of(Role.ROLE_USER))
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getById(1L);

        // Assert
        assertThat(result).isEqualTo(user);
    }

    @Test
    void getById_shouldThrowExceptionIfNotFound() {
        // Arrange
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id= 1 не найден");
    }

    @Test
    void getUserByEmail_shouldReturnUserIfExists() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .roles(Set.of(Role.ROLE_USER))
                .build();

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserByEmail("test@example.com");

        // Assert
        assertThat(result).isEqualTo(user);
    }

    @Test
    void getUserByEmail_shouldReturnNullIfNotFound() {
        // Arrange
        when(userRepo.findByEmail("test@example.com")).thenThrow(new RuntimeException());

        // Act
        User result = userService.getUserByEmail("test@example.com");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void remove_shouldDeleteUserIfExists() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .roles(Set.of(Role.ROLE_USER))
                .build();

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act
        boolean result = userService.remove("test@example.com");

        // Assert
        assertThat(result).isTrue();
        verify(userRepo).deleteById(1L);
    }

    @Test
    void isUsernameAlreadyInUse_shouldReturnTrueIfExists() {
        // Arrange
        when(userRepo.existsUserByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean result = userService.isUsernameAlreadyInUse("test@example.com");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void getListUserRole_shouldReturnRoles() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .roles(Set.of(Role.ROLE_ADMIN))
                .build();

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act
        Set<Role> roles = userService.getListUserRole("test@example.com");

        // Assert
        assertThat(roles).containsExactly(Role.ROLE_ADMIN);
    }

}
