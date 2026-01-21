package com.jewelry.workshop.domain.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Ответ на удаление пользователя")
public class UserDeleteResponseDTO {
    @Schema(description = "Сообщение", example = "Пользователь успешно деактивирован")
    private String message;
}