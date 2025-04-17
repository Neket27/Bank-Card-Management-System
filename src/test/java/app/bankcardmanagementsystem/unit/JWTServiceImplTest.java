package app.bankcardmanagementsystem.unit;

import app.bankcardmanagementsystem.entity.TokenJWT;
import app.bankcardmanagementsystem.entity.User;
import app.bankcardmanagementsystem.repository.TokenRepo;
import app.bankcardmanagementsystem.service.impl.JWTServiceImpl;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JWTServiceImplTest {

    private JWTServiceImpl jwtService;
    private TokenRepo tokenRepo;
    private SecretKey accessSecret;
    private SecretKey refreshSecret;

    @BeforeEach
    void setUp() {
        tokenRepo = mock(TokenRepo.class);

        // Прямо здесь генерируем секреты для теста
        accessSecret = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        refreshSecret = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

        // Чтобы создать экземпляр, понадобится строка в base64
        String accessKeyBase64 = java.util.Base64.getEncoder().encodeToString(accessSecret.getEncoded());
        String refreshKeyBase64 = java.util.Base64.getEncoder().encodeToString(refreshSecret.getEncoded());

        jwtService = new JWTServiceImpl(tokenRepo, accessKeyBase64, refreshKeyBase64);
    }

    @Test
    void generateToken_shouldReturnValidJwtToken() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");

        // Act
        String token = jwtService.generateToken(user);

        // Assert
        assertThat(token).isNotNull();
        String username = jwtService.getUserNameFromAccessToken(token);
        assertThat(username).isEqualTo(user.getEmail());
    }

    @Test
    void generateRefreshToken_shouldReturnValidRefreshToken() {
        // Arrange
        User user = new User();
        user.setEmail("refresh@example.com");

        // Act
        String token = jwtService.generateRefreshToken(new HashMap<>(), user);

        // Assert
        assertThat(token).isNotNull();
        String username = jwtService.getUserNameFromRefreshToken(token);
        assertThat(username).isEqualTo(user.getEmail());
    }

    @Test
    void saveToken_shouldUpdateExistingToken() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("existing@example.com");

        TokenJWT existingToken = new TokenJWT();
        existingToken.setRefreshToken("old_refresh");
        existingToken.setUser(user);

        when(tokenRepo.findById(user.getId())).thenReturn(Optional.of(existingToken));
        when(tokenRepo.save(any(TokenJWT.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TokenJWT result = jwtService.saveToken(user, "new_refresh_token");

        // Assert
        assertThat(result.getRefreshToken()).isEqualTo("new_refresh_token");
        verify(tokenRepo).save(existingToken);
    }

    @Test
    void saveToken_shouldCreateNewTokenIfNotExists() {
        // Arrange
        User user = new User();
        user.setId(2L);
        user.setEmail("new@example.com");

        when(tokenRepo.findById(user.getId())).thenReturn(Optional.empty());
        when(tokenRepo.save(any(TokenJWT.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TokenJWT result = jwtService.saveToken(user, "new_refresh_token");

        // Assert
        assertThat(result.getRefreshToken()).isEqualTo("new_refresh_token");
        assertThat(result.getUser()).isEqualTo(user);
        verify(tokenRepo).save(any(TokenJWT.class));
    }

    @Test
    void getRefreshToken_shouldReturnTokenJWT() {
        // Arrange
        TokenJWT tokenJWT = new TokenJWT();
        tokenJWT.setRefreshToken("some_refresh_token");

        when(tokenRepo.findByRefreshToken("some_refresh_token")).thenReturn(Optional.of(tokenJWT));

        // Act
        TokenJWT result = jwtService.getRefreshToken("some_refresh_token");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRefreshToken()).isEqualTo("some_refresh_token");
    }

    @Test
    void removeRefreshToken_shouldCallRepositoryDelete() {
        // Arrange
        String refreshToken = "refresh_to_delete";

        // Act
        jwtService.removeRefreshToken(refreshToken);

        // Assert
        verify(tokenRepo).deleteByRefreshToken(refreshToken);
    }

    @Test
    void isTokenValidAccessToken_shouldReturnTrueForValidToken() {
        // Arrange
        User user = new User();
        user.setEmail("access@test.com");
        String token = jwtService.generateToken(user);

        // Act
        boolean isValid = jwtService.isTokenValidAccessToken(token, user);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValidRefreshToken_shouldReturnTrueForValidToken() {
        // Arrange
        User user = new User();
        user.setEmail("refresh@test.com");
        String token = jwtService.generateRefreshToken(new HashMap<>(), user);

        // Act
        boolean isValid = jwtService.isTokenValidRefreshToken(token, user);

        // Assert
        assertThat(isValid).isTrue();
    }

}
