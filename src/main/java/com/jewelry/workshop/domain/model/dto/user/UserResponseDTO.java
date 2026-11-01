package com.jewelry.workshop.domain.model.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO для ответа с информацией о пользователе")
public class UserResponseDTO {

    @Schema(description = "ID пользователя", example = "1")
    private Long id;

    @Schema(description = "Имя пользователя", example = "ivan_petrov")
    private String username;

    @Schema(description = "Email пользователя", example = "ivan.petrov@example.com")
    private String email;

    @Schema(description = "Роль пользователя", example = "CLIENT")
    private String role;

    @Schema(description = "Активен ли пользователь", example = "true")
    private Boolean enabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата создания", example = "2024-01-15 14:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата обновления", example = "2024-01-15 14:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "ID связанного клиента (если есть)", example = "1")
    private Long clientId;

    @Schema(description = "ID связанного сотрудника (если есть)", example = "1")
    private Long employeeId;
}