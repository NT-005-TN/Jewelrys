package com.jewelry.workshop.domain.model.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName = "builder", access = AccessLevel.PUBLIC)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Ответ на аутентификацию (токены или сообщение)")
public class AuthResponseDTO {

    @Schema(description = "Токен доступа (access_token)")
    private String accessToken;

    @Schema(description = "Токен обновления (refresh_token)")
    private String refreshToken;

    @Schema(description = "Время жизни токена в секундах")
    private Long expiresIn;

    @Schema(description = "Информация о пользователе")
    private AuthUserDTO user;

    @Schema(description = "Сообщение (например, при регистрации)")
    private String message;
}