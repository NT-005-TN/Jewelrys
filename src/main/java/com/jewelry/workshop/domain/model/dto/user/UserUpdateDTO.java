package com.jewelry.workshop.domain.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO для обновления пользователя")
public class UserUpdateDTO {

    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    @Schema(description = "Имя пользователя", example = "ivan_petrov_updated")
    private String username;

    @Email(message = "Некорректный формат email")
    @Schema(description = "Email пользователя", example = "ivan.updated@example.com")
    private String email;

    @Schema(description = "Активен ли пользователь", example = "true")
    private Boolean enabled;

    @Schema(description = "Роль пользователя", example = "SELLER",
            allowableValues = {"CLIENT", "SELLER", "ADMIN"})
    private String role;
}