package com.jewelry.workshop.domain.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Данные для импорта пользователя")
public class UserImportDTO {

    @Schema(
            description = "Email пользователя",
            example = "ivan.petrov@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @Schema(
            description = "Имя пользователя (логин)",
            example = "ivan_petrov",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @Schema(
            description = "Роль пользователя",
            allowableValues = {"CLIENT", "SELLER", "ADMIN"},
            example = "CLIENT",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String role;

    @Schema(
            description = "Пароль в открытом виде (будет захеширован при сохранении)",
            example = "SecurePass123!",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;
}