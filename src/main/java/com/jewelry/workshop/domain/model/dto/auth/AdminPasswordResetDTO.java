package com.jewelry.workshop.domain.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO для сброса пароля администратором")
public class AdminPasswordResetDTO {
    @NotBlank
    @Size(min = 8, message = "Пароль должен быть минимум 8 символов")
    @Schema(description = "Новый пароль", example = "NewSecurePass123!", required = true)
    private String newPassword;
}