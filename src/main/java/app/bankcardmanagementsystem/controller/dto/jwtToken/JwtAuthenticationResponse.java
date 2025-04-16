package app.bankcardmanagementsystem.controller.dto.jwtToken;

import app.bankcardmanagementsystem.controller.dto.user.UserDto;
import lombok.Builder;

@Builder
public record JwtAuthenticationResponse(
        String accessToken,
        String refreshToken,
        UserDto user
) {}

