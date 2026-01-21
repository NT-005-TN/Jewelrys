package com.jewelry.workshop.domain.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO для выхода из системы")
public class LogoutRequestDTO {

    @NotBlank(message = "Refresh токен обязателен")
    @Schema(description = "Refresh токен для инвалидации", required = true)
    private String refreshToken;
}