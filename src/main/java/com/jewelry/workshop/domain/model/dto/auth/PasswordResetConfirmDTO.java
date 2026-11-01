package com.jewelry.workshop.domain.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Подтверждение сброса пароля")
public class PasswordResetConfirmDTO {

    @NotBlank(message = "Токен сброса обязателен")
    @Size(max = 512, message = "Некорректная длина токена")
    private String token;

    @NotBlank(message = "Новый пароль обязателен")
    @Size(min = 12, max = 128, message = "Пароль должен содержать от 12 до 128 символов")
    private String newPassword;

    @NotBlank(message = "Подтверждение пароля обязательно")
    @Size(max = 128, message = "Подтверждение пароля не должно превышать 128 символов")
    private String confirmPassword;
}
