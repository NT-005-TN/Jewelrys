package com.jewelry.workshop.domain.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Подтверждение email одноразовым токеном")
public class EmailVerificationDTO {

    @NotBlank(message = "Токен подтверждения обязателен")
    @Size(max = 512, message = "Некорректная длина токена")
    private String token;
}
