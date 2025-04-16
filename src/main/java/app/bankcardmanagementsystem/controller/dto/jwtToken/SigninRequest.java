package app.bankcardmanagementsystem.controller.dto.jwtToken;

import jakarta.validation.constraints.NotBlank;

public record SigninRequest(

        @NotBlank(message = "password = null")
        String password,

        @NotBlank(message = "email =null")
        String email
) {
}