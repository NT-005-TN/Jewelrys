package com.jewelry.workshop.domain.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO для запроса входа в систему")
public class LoginRequestDTO {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @Schema(description = "Email пользователя", example = "ivan.petrov@example.com", required = true)
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(max = 128, message = "Пароль не должен превышать 128 символов")
    @Schema(description = "Пароль", example = "password123", required = true)
    private String password;
}

