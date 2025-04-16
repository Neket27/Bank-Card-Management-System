package app.bankcardmanagementsystem.controller.dto.jwtToken;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RefreshTokenRequest {
    private String token;
}
