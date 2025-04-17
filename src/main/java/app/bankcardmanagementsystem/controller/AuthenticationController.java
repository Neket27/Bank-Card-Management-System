package app.bankcardmanagementsystem.controller;


import app.bankcardmanagementsystem.controller.advice.annotation.CustomExceptionHandler;
import app.bankcardmanagementsystem.controller.dto.jwtToken.JwtAuthenticationResponse;
import app.bankcardmanagementsystem.controller.dto.jwtToken.ResetPasswordDto;
import app.bankcardmanagementsystem.controller.dto.jwtToken.SignUpRequest;
import app.bankcardmanagementsystem.controller.dto.jwtToken.SigninRequest;
import app.bankcardmanagementsystem.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Авторизация", description = "Действия связанные с аутентификацией")
@CustomExceptionHandler
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Позволяет создать нового пользователя и получить JWT токен аутентификации"
    )
    @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован",
            content = @Content(schema = @Schema(implementation = JwtAuthenticationResponse.class)))
    @PostMapping(path = "/singup")
    public ResponseEntity<JwtAuthenticationResponse> singup(@RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authenticationService.signnup(signUpRequest));
    }

    @Operation(
            summary = "Аутентификация пользователя",
            description = "Позволяет пользователю войти в систему и получить JWT токен"
    )
    @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
            content = @Content(schema = @Schema(implementation = JwtAuthenticationResponse.class)))
    @ApiResponse(responseCode = "403", description = "Неверные учетные данные")
    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponse> signin(@RequestBody SigninRequest signinRequest) {
        JwtAuthenticationResponse jwtAuthenticationResponse = authenticationService.signin(signinRequest);
        return ResponseEntity.ok(jwtAuthenticationResponse);
    }

    @Operation(
            summary = "Обновление токена доступа",
            description = "Позволяет пользователю получить новый токен доступа по refresh токену",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Токен успешно обновлен",
            content = @Content(schema = @Schema(implementation = JwtAuthenticationResponse.class)))
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/refresh")
    public JwtAuthenticationResponse refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken,
                                             HttpServletRequest httpServletRequest,
                                             HttpServletResponse httpServletResponse) throws ServletException {
        return authenticationService.refreshToken(refreshToken, httpServletRequest, httpServletResponse);
    }

    @Operation(
            summary = "Выход из системы",
            description = "Позволяет пользователю выйти из системы, аннулируя refresh токен",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Пользователь успешно вышел из системы")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "refreshToken") String refreshTokenFromCooke,
                                    HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse) throws ServletException {
        authenticationService.logout(refreshTokenFromCooke, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok("Вы вышли из системы");
    }

    @Operation(
            summary = "Сброс пароля",
            description = "Позволяет пользователю сбросить пароль и получить новый JWT токен",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Пароль успешно сброшен",
            content = @Content(schema = @Schema(implementation = JwtAuthenticationResponse.class)))
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping("/password/reset")
    public ResponseEntity<JwtAuthenticationResponse> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        return ResponseEntity.ok(authenticationService.resetPassword(resetPasswordDto));
    }


}
