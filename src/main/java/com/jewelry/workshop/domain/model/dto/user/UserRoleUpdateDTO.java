package com.jewelry.workshop.domain.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO для изменения роли пользователя")
public class UserRoleUpdateDTO {

    @NotNull(message = "Роль обязательна")
    @Schema(description = "Новая роль", example = "SELLER",
            allowableValues = {"CLIENT", "SELLER", "ADMIN"})
    private String role;
}