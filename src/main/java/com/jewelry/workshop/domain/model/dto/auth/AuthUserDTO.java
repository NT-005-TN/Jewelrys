package com.jewelry.workshop.domain.model.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder(builderMethodName = "builder", access = AccessLevel.PUBLIC)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Информация о пользователе в ответе аутентификации")
public class AuthUserDTO {

    @Schema(description = "ID пользователя", example = "1")
    private Long id;

    @Schema(description = "Email", example = "ivan.petrov@example.com")
    private String email;

    @Schema(description = "Роль", allowableValues = {"CLIENT", "SELLER", "ADMIN"}, example = "CLIENT")
    private String role;

    @Schema(description = "Полное имя", example = "Петров Иван Сергеевич")
    private String fullName;
}