package app.bankcardmanagementsystem.controller.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UpdateDataUserDto {
    private String email;
}