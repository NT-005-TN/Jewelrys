package com.jewelry.workshop.domain.model.dto.client;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(builderMethodName = "builder", access = AccessLevel.PUBLIC)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO для ответа с информацией о клиенте")
public class ClientResponseDTO {

    @Schema(description = "ID клиента", example = "1")
    private Long id;

    @Schema(description = "Имя клиента", example = "Иван")
    private String firstName;

    @Schema(description = "Фамилия клиента", example = "Петров")
    private String lastName;

    @Schema(description = "Отчество клиента", example = "Сергеевич")
    private String patronymic;

    @Schema(description = "Email клиента", example = "ivan.petrov@example.com")
    private String email;

    @Schema(description = "Телефон клиента", example = "+79991234567")
    private String phone;

    @Schema(description = "Является ли клиент постоянным", example = "false")
    private Boolean isPermanent;

    @Schema(description = "Полное имя клиента", example = "Петров Иван Сергеевич")
    private String fullName;

    @Schema(description = "ID пользователя", example = "1")
    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата создания", example = "2024-01-15 14:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата обновления", example = "2024-01-15 14:30:00")
    private LocalDateTime updatedAt;
}