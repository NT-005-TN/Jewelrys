package com.jewelry.workshop.domain.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO для запроса обновления access-токена")
public class RefreshTokenRequestDTO {

    @NotBlank(message = "Refresh токен обязателен")
    @Schema(
            description = "Токен обновления (refresh_token), полученный при входе в систему",
            example = "eyJhbGciOiJIUzUxMiJ9.xxxxx",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String refreshToken;
}