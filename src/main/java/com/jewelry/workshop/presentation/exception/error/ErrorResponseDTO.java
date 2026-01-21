package com.jewelry.workshop.presentation.exception.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Стандартный ответ при ошибке")
public class ErrorResponseDTO {
    @Schema(description = "Временная метка ошибки", example = "2026-01-11T15:30:45")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP статус", example = "400")
    private int status;

    @Schema(description = "Код ошибки (опционально)", example = "EMAIL_ALREADY_USED")
    private String errorCode;

    @Schema(description = "Сообщение об ошибке", example = "Email уже используется")
    private String message;

    @Schema(description = "Путь запроса", example = "/api/clients/me")
    private String path;
}