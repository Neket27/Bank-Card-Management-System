package app.bankcardmanagementsystem.unit;

import app.bankcardmanagementsystem.controller.dto.jwtToken.JwtAuthenticationResponse;
import app.bankcardmanagementsystem.controller.dto.jwtToken.ResetPasswordDto;
import app.bankcardmanagementsystem.controller.dto.jwtToken.SignUpRequest;
import app.bankcardmanagementsystem.controller.dto.jwtToken.SigninRequest;
import app.bankcardmanagementsystem.controller.dto.user.CreateUserDto;
import app.bankcardmanagementsystem.controller.dto.user.UpdateUserDto;
import app.bankcardmanagementsystem.entity.Role;
import app.bankcardmanagementsystem.entity.TokenJWT;
import app.bankcardmanagementsystem.entity.User;
import app.bankcardmanagementsystem.exception.UpdateException;
import app.bankcardmanagementsystem.mapper.UserMapper;
import app.bankcardmanagementsystem.service.CookeService;
import app.bankcardmanagementsystem.service.JWTService;
import app.bankcardmanagementsystem.service.UserService;
import app.bankcardmanagementsystem.service.impl.AuthenticationServiceImpl;
import app.bankcardmanagementsystem.utils.jwtToken.EncoderPassword;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AuthenticationServiceImplTest {

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @Mock
    private CookeService cookeService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticationService = new AuthenticationServiceImpl(userService, userMapper, authenticationManager, jwtService, cookeService);
    }

    @Test
    void signup_existingUser_shouldSignin() {
        // Arrange
        SignUpRequest signUpRequest = new SignUpRequest("test@example.com", "password", Set.of(Role.ROLE_USER));
        User user = new User();
        when(userService.getUserByEmail(signUpRequest.email())).thenReturn(user);
        when(userService.getUserByEmail(signUpRequest.email())).thenReturn(user);
        when(userService.getUserByEmail(anyString())).thenReturn(user);
        // Act
        JwtAuthenticationResponse response = authenticationService.signnup(signUpRequest);

        // Assert
        assertThat(response).isNotNull();
    }

    @Test
    void signup_newUser_shouldCreateAndSignin() {
        // Arrange
        SignUpRequest signUpRequest = new SignUpRequest("new@example.com", "password", Set.of(Role.ROLE_USER));
        User user = User.builder().email("new@example.com").password("password").build();
        when(userService.getUserByEmail(signUpRequest.email())).thenReturn(user);
        when(userMapper.toCreateUserDto(signUpRequest)).thenReturn(any(CreateUserDto.class));

        // Act
        JwtAuthenticationResponse response = authenticationService.signnup(signUpRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(userService).createUser(any());
    }

    @Test
    void signin_validCredentials_shouldReturnTokens() {
        // Arrange
        SigninRequest request = new SigninRequest("test@example.com", "password");
        User user = new User();
        when(userService.getUserByEmail(request.email())).thenReturn(user);

        // Act
        JwtAuthenticationResponse response = authenticationService.signin(request);

        // Assert
        assertThat(response).isNotNull();
    }

    @Test
    void signin_invalidCredentials_shouldThrowAuthenticationServiceException() {
        // Arrange
        SigninRequest request = new SigninRequest("test@example.com", "wrongPassword");
        doThrow(new AuthenticationServiceException("Invalid email or password"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act + Assert
        assertThatThrownBy(() -> authenticationService.signin(request))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void refreshToken_validToken_shouldReturnNewTokens() {
        // Arrange
        String refreshToken = "valid_refresh_token";
        User user = new User();
        when(jwtService.getUserNameFromRefreshToken(refreshToken)).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);
        when(jwtService.isTokenValidRefreshToken(refreshToken, user)).thenReturn(true);
        when(jwtService.getRefreshToken(refreshToken)).thenReturn(TokenJWT.builder().refreshToken("new_refresh_token").build());
        doNothing().when(cookeService).createCooke(any());


        // Act
        JwtAuthenticationResponse response = authenticationService.refreshToken(refreshToken, httpServletRequest, httpServletResponse);

        // Assert
        assertThat(response).isNotNull();
        verify(cookeService, times(2)).createCooke(any());
    }

    @Test
    void refreshToken_invalidToken_shouldReturnNull() {
        // Arrange
        String refreshToken = "invalid_refresh_token";
        when(jwtService.getUserNameFromRefreshToken(refreshToken)).thenReturn("test@example.com");
        when(userService.getUserByEmail("test@example.com")).thenReturn(new User());
        when(jwtService.isTokenValidRefreshToken(refreshToken, new User())).thenReturn(false);

        // Act
        JwtAuthenticationResponse response = authenticationService.refreshToken(refreshToken, httpServletRequest, httpServletResponse);

        // Assert
        assertThat(response).isNull();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAuthenticationInfo_shouldReturnUser() {
        // Arrange
        User user = new User();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        User result = authenticationService.getAuthenticationInfo();

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    void logout_shouldCallLogoutAndRemoveRefreshToken() throws ServletException {
        // Arrange
        String refreshToken = "refreshToken";

        // Act
        authenticationService.logout(refreshToken, httpServletRequest, httpServletResponse);

        // Assert
        verify(httpServletRequest).logout();
        verify(jwtService).removeRefreshToken(refreshToken);
    }

    @Test
    void resetPassword_correctOldPassword_shouldUpdatePassword() {
        // Arrange
        User user = new User();
        user.setPassword(EncoderPassword.encode("oldPassword"));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("oldPassword", "newPassword");

        // Act
        JwtAuthenticationResponse response = authenticationService.resetPassword(resetPasswordDto);

        // Assert
        assertThat(response).isNotNull();
        verify(userService).updateDataUser(any(UpdateUserDto.class));
    }

    @Test
    void resetPassword_wrongOldPassword_shouldThrowUpdateException() {
        // Arrange
        User user = new User();
        user.setPassword(EncoderPassword.encode("correctPassword"));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("wrongPassword", "newPassword");

        // Act + Assert
        assertThatThrownBy(() -> authenticationService.resetPassword(resetPasswordDto))
                .isInstanceOf(UpdateException.class)
                .hasMessageContaining("Отправленный пароль и пароль авторизированного пользователя не совпадают");
    }
}
