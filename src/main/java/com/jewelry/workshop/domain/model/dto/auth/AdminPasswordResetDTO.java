package com.jewelry.workshop.domain.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO для сброса пароля администратором")
public class AdminPasswordResetDTO {
    @NotBlank
    @Size(min = 12, max = 128, message = "Пароль должен содержать от 12 до 128 символов")
    @Schema(description = "Новый пароль", example = "NewSecurePass123!", required = true)
    private String newPassword;
}
