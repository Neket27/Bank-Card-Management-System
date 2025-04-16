package app.bankcardmanagementsystem.repository;

import app.bankcardmanagementsystem.entity.TokenJWT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepo extends JpaRepository<TokenJWT, Long> {

    void deleteByRefreshToken(String refreshToken);

    Optional<TokenJWT> findByRefreshToken(String refreshToken);

}
