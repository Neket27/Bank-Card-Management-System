package app.bankcardmanagementsystem.service;

import app.bankcardmanagementsystem.entity.TokenJWT;
import app.bankcardmanagementsystem.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.HashMap;

public interface JWTService {

    String generateToken(UserDetails userDetails);

    String generateRefreshToken(HashMap<String, UserDetails> extraClaims, UserDetails userDetails);

    TokenJWT saveToken(User user, String refreshToken);

    String getUserNameFromAccessToken(String token);

    String getUserNameFromRefreshToken(String token);

    String extractUserName(String token, SecretKey secretKey);

    boolean isTokenValidAccessToken(String token, UserDetails userDetails);

    boolean isTokenValidRefreshToken(String token, UserDetails userDetails);

    @Transactional
    TokenJWT getRefreshToken(String refreshToken);

    void removeRefreshToken(String refreshToken);
}
