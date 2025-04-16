package app.bankcardmanagementsystem.controller.dto.user;

import app.bankcardmanagementsystem.entity.Role;
import lombok.Builder;

import java.util.Set;

@Builder
public record UpdateUserDto(
        String password,
        String email,
        Set<Role> roles) {
}
