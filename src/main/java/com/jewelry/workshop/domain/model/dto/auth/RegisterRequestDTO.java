package com.jewelry.workshop.domain.model.dto.auth;

import com.jewelry.workshop.domain.model.dto.client.BaseClientInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "DTO для запроса регистрации нового клиента")
public class RegisterRequestDTO extends BaseClientInfoDTO {

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 12, max = 128, message = "Пароль должен содержать от 12 до 128 символов")
    @Schema(description = "Пароль", example = "correct horse battery staple", required = true)
    private String password;

    @NotBlank(message = "Подтверждение пароля обязательно")
    @Size(max = 128, message = "Подтверждение пароля не должно превышать 128 символов")
    @Schema(description = "Подтверждение пароля", example = "correct horse battery staple", required = true)
    private String confirmPassword;
}
