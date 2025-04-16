package app.bankcardmanagementsystem.service.impl;


import app.bankcardmanagementsystem.controller.dto.cooke.CreateCookeDto;
import app.bankcardmanagementsystem.controller.dto.jwtToken.JwtAuthenticationResponse;
import app.bankcardmanagementsystem.controller.dto.jwtToken.ResetPasswordDto;
import app.bankcardmanagementsystem.controller.dto.jwtToken.SignUpRequest;
import app.bankcardmanagementsystem.controller.dto.jwtToken.SigninRequest;
import app.bankcardmanagementsystem.controller.dto.user.CreateUserDto;
import app.bankcardmanagementsystem.controller.dto.user.UpdateUserDto;
import app.bankcardmanagementsystem.entity.User;
import app.bankcardmanagementsystem.exception.UpdateException;
import app.bankcardmanagementsystem.mapper.UserMapper;
import app.bankcardmanagementsystem.service.AuthenticationService;
import app.bankcardmanagementsystem.service.CookeService;
import app.bankcardmanagementsystem.service.JWTService;
import app.bankcardmanagementsystem.service.UserService;
import app.bankcardmanagementsystem.utils.jwtToken.EncoderPassword;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final CookeService cookeService;
    @Value("${cooke.time.live.refreshToken}")
    private int cookeTimeLive;

    @Override
    public JwtAuthenticationResponse signnup(SignUpRequest signUpRequest) {

        User user = userService.getUserByEmail(signUpRequest.email());
        if (user != null)
            signin(new SigninRequest(signUpRequest.password(), signUpRequest.email()));

        CreateUserDto createUserDto = userMapper.toCreateUserDto(signUpRequest);
        userService.createUser(createUserDto);
        return signin(new SigninRequest(signUpRequest.password(), signUpRequest.email()));

    }

    @Override
    public JwtAuthenticationResponse signin(SigninRequest signinRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signinRequest.email(), signinRequest.password()));
        User user = userService.getUserByEmail(signinRequest.email());
        if (user == null)
            return null;

        return createJwtAuthenticationResponse(user);
    }

    @Override
    public JwtAuthenticationResponse refreshToken(String refreshToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)  {
        if (refreshToken == null || refreshToken.isEmpty())
            return JwtAuthenticationResponse.builder().build();

        String username = jwtService.getUserNameFromRefreshToken(refreshToken);
        User user = userService.getUserByEmail(username);
        if (jwtService.isTokenValidRefreshToken(refreshToken, user) && jwtService.getRefreshToken(refreshToken) != null) {
            createCooke(refreshToken);
            return createJwtAuthenticationResponse(user);
        }
        return null;
    }

    @Override
    public User getAuthenticationInfo() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public void logout(String refreshToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        httpServletRequest.logout();
        jwtService.removeRefreshToken(refreshToken);
    }

    @Override
    public JwtAuthenticationResponse resetPassword(ResetPasswordDto resetPasswordDto) {
        User authenticationUser = getAuthenticationInfo();
        if (EncoderPassword.equalsPasswords(resetPasswordDto.getPassword(), authenticationUser.getPassword())) {
            authenticationUser.setPassword(EncoderPassword.encode(resetPasswordDto.getNewPassword()));

            UpdateUserDto updateUserDto = UpdateUserDto.builder()
                    .email(authenticationUser.getUsername())
                    .password(resetPasswordDto.getNewPassword())
                    .roles(authenticationUser.getRoles())
                    .build();

            userService.updateDataUser(updateUserDto);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationUser.getUsername(), resetPasswordDto.getNewPassword()));
        } else {
            throw new UpdateException("Отправленный пароль и пароль авторизированного пользователя не совпадают. Изменение пароля не произошло");
        }
        return createJwtAuthenticationResponse(authenticationUser);
    }

    private JwtAuthenticationResponse createJwtAuthenticationResponse(User user) {
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
        jwtService.saveToken(user, refreshToken);

        createCooke(refreshToken);

        return JwtAuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .user(userMapper.toDto(user))
                .build();
    }

    private void createCooke(String refreshToken) {
        CreateCookeDto createCookeDto = CreateCookeDto.builder()
                .key("refreshToken")
                .data(refreshToken)
                .timeLiveCooke(cookeTimeLive)
                .path("/")
                .contentType("text/plain")
                .build();
        cookeService.createCooke(createCookeDto);
    }

}
