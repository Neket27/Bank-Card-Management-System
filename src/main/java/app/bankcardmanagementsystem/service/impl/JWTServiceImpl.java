package app.bankcardmanagementsystem.service.impl;

import app.bankcardmanagementsystem.entity.TokenJWT;
import app.bankcardmanagementsystem.entity.User;
import app.bankcardmanagementsystem.repository.TokenRepo;
import app.bankcardmanagementsystem.service.JWTService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.security.SignatureException;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Service
@Slf4j
@Setter
@Getter
public class JWTServiceImpl implements JWTService {

    private  final TokenRepo tokenRepo;
    private final SecretKey jwtAccessSecret;
    private final SecretKey jwtRefreshSecret;
    public JWTServiceImpl(TokenRepo tokenRepo, @Value("${jwt.secret.access}") String jwtAccessSecret, @Value("${jwt.secret.refresh}") String jwtRefreshSecret) {
        this.tokenRepo = tokenRepo;
        this.jwtAccessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
        this.jwtRefreshSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret));

    }

    @Override
    public String generateToken(@NonNull UserDetails userDetails){
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()*1000*60*24))
                .signWith(jwtAccessSecret, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(@NonNull HashMap<String, UserDetails> extraClaims, @NonNull UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+604800000))
                .signWith(jwtRefreshSecret, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    @Transactional
    public TokenJWT saveToken(User user, String refreshToken){
        TokenJWT tokenJWT = tokenRepo.findById(user.getId()).orElse(null);
        if(tokenJWT!=null){
            tokenJWT.setRefreshToken(refreshToken);
            return tokenRepo.save(tokenJWT);
        }
        return tokenRepo.save(new TokenJWT(user.getId(),refreshToken,user));
    }

    @Override
    @Transactional
    public TokenJWT getRefreshToken(String refreshToken) {
        TokenJWT tokenJWT = tokenRepo.findByRefreshToken(refreshToken).orElse(null);//.orElseThrow(() -> new ErrorException("RefreshToken не найден в базе данных"));
        return tokenJWT;
    }

    @Override
    @Transactional
    public void removeRefreshToken(String refreshToken){
        tokenRepo.deleteByRefreshToken(refreshToken);
    }

    @Override
    public String getUserNameFromAccessToken(@NonNull String token) {
        return extractClaim(token,jwtAccessSecret,Claims::getSubject);
    }

    @Override
    public String getUserNameFromRefreshToken(@NonNull String token){
        return extractClaim(token,jwtRefreshSecret,Claims::getSubject);
    }

    @Override
    public String extractUserName(@NonNull String token, @NonNull SecretKey secretKey) {
        return extractClaim(token,secretKey,Claims::getSubject);
    }

    private <T> T extractClaim(@NonNull String token,@NonNull SecretKey secretKey, Function<Claims,T> claimsResolvers){
        final Claims claims= getClaims(token,secretKey);
        return claimsResolvers.apply(claims);
    }

    private Claims getClaims(@NonNull String token, @NonNull SecretKey secretKey) {
        try {
          return   Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }
      catch (ExpiredJwtException expEx) {
        log.error("Token expired", expEx);
    } catch (UnsupportedJwtException unsEx) {
        log.error("Unsupported jwt", unsEx);
    } catch (MalformedJwtException mjEx) {
        log.error("Malformed jwt", mjEx);
    } catch (Exception e) {
        log.error("invalid token", e);
    }
        return null;
    }

    @Override
    public boolean isTokenValidAccessToken(@NonNull String token, @NonNull UserDetails userDetails){
        final String username =getUserNameFromAccessToken(token);
        return (username.equals(userDetails.getUsername())&&!isTokenExpired(token,jwtAccessSecret));
    }

    @Override
    public boolean isTokenValidRefreshToken(@NonNull String token, @NonNull UserDetails userDetails){
        final String username =getUserNameFromRefreshToken(token);
        return (username.equals(userDetails.getUsername())&&!isTokenExpired(token,jwtRefreshSecret));
    }

    private boolean isTokenExpired(@NonNull String token,@NonNull SecretKey secretKey) {
      return  extractClaim(token,secretKey, Claims::getExpiration).before(new Date());
    }

}
