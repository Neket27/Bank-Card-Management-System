package app.bankcardmanagementsystem.controller;


import app.bankcardmanagementsystem.controller.advice.annotation.CustomExceptionHandler;
import app.bankcardmanagementsystem.controller.dto.jwtToken.JwtAuthenticationResponse;
import app.bankcardmanagementsystem.controller.dto.jwtToken.ResetPasswordDto;
import app.bankcardmanagementsystem.controller.dto.jwtToken.SignUpRequest;
import app.bankcardmanagementsystem.controller.dto.jwtToken.SigninRequest;
import app.bankcardmanagementsystem.service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CustomExceptionHandler
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping(path = "/singup")
    public ResponseEntity<JwtAuthenticationResponse> singup(@RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authenticationService.signnup(signUpRequest));

    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponse> signin(@RequestBody SigninRequest signinRequest) {
        JwtAuthenticationResponse jwtAuthenticationResponse = authenticationService.signin(signinRequest);
        if (jwtAuthenticationResponse == null)
            return ResponseEntity.status(403).build();

        return ResponseEntity.ok(jwtAuthenticationResponse);
    }

    @GetMapping("/refresh")
    public JwtAuthenticationResponse refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken,HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        return authenticationService.refreshToken(refreshToken, httpServletRequest, httpServletResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "refreshToken") String refreshTokenFromCooke, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        authenticationService.logout(refreshTokenFromCooke, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok("Вы вышли из системы");
    }

    @PostMapping("/password/reset")
    public ResponseEntity<JwtAuthenticationResponse> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto){
        return ResponseEntity.ok(authenticationService.resetPassword(resetPasswordDto));
    }

}
