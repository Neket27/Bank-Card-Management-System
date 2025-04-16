package app.bankcardmanagementsystem.service;

import app.bankcardmanagementsystem.controller.dto.jwtToken.JwtAuthenticationResponse;
import app.bankcardmanagementsystem.controller.dto.jwtToken.ResetPasswordDto;
import app.bankcardmanagementsystem.controller.dto.jwtToken.SignUpRequest;
import app.bankcardmanagementsystem.controller.dto.jwtToken.SigninRequest;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {
    JwtAuthenticationResponse signnup(SignUpRequest signUpRequest);
    JwtAuthenticationResponse signin(SigninRequest signinRequest);
    JwtAuthenticationResponse refreshToken(String token,HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException;
    UserDetails getAuthenticationInfo();
    void logout(String refreshToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException;
    JwtAuthenticationResponse resetPassword(ResetPasswordDto resetPasswordDto);
}
