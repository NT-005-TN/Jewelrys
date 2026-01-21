package com.jewelry.workshop.domain.model.dto.auth;

import com.jewelry.workshop.domain.model.dto.client.BaseClientInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO для запроса регистрации нового клиента")
public class RegisterRequestDTO extends BaseClientInfoDTO {

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    @Schema(description = "Пароль", example = "password123", required = true)
    private String password;

    @NotBlank(message = "Подтверждение пароля обязательно")
    @Schema(description = "Подтверждение пароля", example = "password123", required = true)
    private String confirmPassword;
}