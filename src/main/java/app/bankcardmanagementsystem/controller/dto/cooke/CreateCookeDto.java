package app.bankcardmanagementsystem.controller.dto.cooke;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class CreateCookeDto {
    private String key;
    private String data;
    private int timeLiveCooke;
    private String path;
    private String contentType;

}
