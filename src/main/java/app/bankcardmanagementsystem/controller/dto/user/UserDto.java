package app.bankcardmanagementsystem.controller.dto.user;

import app.bankcardmanagementsystem.entity.Role;

import java.util.Set;

public record UserDto(
        Long id,
        String email,
        Set<Role> roles
) {
}
