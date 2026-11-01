package com.jewelry.workshop.domain.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO для запроса сброса пароля")
public class PasswordResetRequestDTO {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @Size(max = 254, message = "Email не должен превышать 254 символа")
    @Schema(description = "Email пользователя", example = "ivan.petrov@example.com", required = true)
    private String email;
}
