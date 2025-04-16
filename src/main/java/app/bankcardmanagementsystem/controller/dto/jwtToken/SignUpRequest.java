package app.bankcardmanagementsystem.controller.dto.jwtToken;

import app.bankcardmanagementsystem.entity.Role;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record SignUpRequest(

        @NotBlank(message = "password user = null")
        String password,

        @NotBlank(message = "email = null")
        String email,

        Set<Role> roles
) {
}
