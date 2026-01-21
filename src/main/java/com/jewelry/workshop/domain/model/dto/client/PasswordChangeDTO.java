package com.jewelry.workshop.domain.model.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO для смены пароля")
public class PasswordChangeDTO {

    @NotBlank(message = "Текущий пароль обязателен")
    @Size(min = 6, max = 100, message = "Пароль должен быть от 6 до 100 символов")
    @Schema(description = "Текущий пароль", example = "oldPass123")
    private String currentPassword;

    @NotBlank(message = "Новый пароль обязателен")
    @Size(min = 8, max = 100, message = "Новый пароль должен быть от 8 до 100 символов")
    @Schema(description = "Новый пароль", example = "newSecurePass456!")
    private String newPassword;

    @NotBlank(message = "Подтверждение нового пароля обязательно")
    @Schema(description = "Подтверждение нового пароля", example = "newSecurePass456!")
    private String confirmPassword;
}