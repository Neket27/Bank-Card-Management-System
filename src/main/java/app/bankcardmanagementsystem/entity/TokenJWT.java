package app.bankcardmanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "token_jwt")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class TokenJWT {
    @Id
    private Long userId;
    private String refreshToken;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private User user;
}
