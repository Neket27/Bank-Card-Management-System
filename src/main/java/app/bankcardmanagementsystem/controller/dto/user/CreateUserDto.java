package app.bankcardmanagementsystem.controller.dto.user;

import app.bankcardmanagementsystem.entity.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Set;

@Builder
public record CreateUserDto(
        @NotBlank(message = "password user = null")
        String password,

        @NotBlank(message = "email user = null")
        String email,

        Set<Role> roles
) {
}
