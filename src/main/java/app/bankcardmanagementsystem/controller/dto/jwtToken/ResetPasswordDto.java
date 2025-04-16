package app.bankcardmanagementsystem.controller.dto.jwtToken;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Builder
public class ResetPasswordDto {
    private final String password;
    private final String newPassword;
}
