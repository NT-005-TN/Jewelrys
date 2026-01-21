package com.jewelry.workshop.domain.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO для создания пользователя")
public class UserCreateDTO {

    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    @Schema(description = "Имя пользователя", example = "ivan_petrov", required = true)
    private String username;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @Schema(description = "Email пользователя", example = "ivan.petrov@example.com", required = true)
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    @Schema(description = "Пароль", example = "password123", required = true)
    private String password;

    @NotNull(message = "Роль обязательна")
    @Schema(description = "Роль пользователя", example = "CLIENT", required = true,
            allowableValues = {"CLIENT", "SELLER", "ADMIN"})
    private String role;
}