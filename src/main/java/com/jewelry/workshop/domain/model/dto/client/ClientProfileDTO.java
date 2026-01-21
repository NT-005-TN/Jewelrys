package com.jewelry.workshop.domain.model.dto.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO для профиля клиента (упрощенный для /me endpoint)")
public class ClientProfileDTO {

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
    private Boolean isPermanent = Boolean.FALSE;

    @Schema(description = "Полное имя клиента", example = "Петров Иван Сергеевич")
    private String fullName;

    @Schema(description = "Количество заказов", example = "5")
    private Long orderCount;

    @Schema(description = "Общая сумма всех заказов", example = "150000.00")
    private BigDecimal totalSpent;

    @Schema(description = "Дата последнего заказа", example = "2024-01-20 14:30:00")
    private LocalDateTime lastOrderDate;
}