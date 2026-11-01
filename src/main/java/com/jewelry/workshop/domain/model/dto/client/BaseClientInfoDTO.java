package com.jewelry.workshop.domain.model.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BaseClientInfoDTO {
    @NotBlank @Size(min = 2, max = 50)
    @Schema(description = "Имя клиента", example = "Иван", required = true)
    private String firstName;

    @NotBlank @Size(min = 2, max = 50)
    @Schema(description = "Фамилия клиента", example = "Петров", required = true)
    private String lastName;

    @Size(max = 50)
    @Schema(description = "Отчество клиента", example = "Сергеевич")
    private String patronymic;

    @NotBlank @Email
    @Schema(description = "Email клиента", example = "ivan.petrov@example.com", required = true)
    private String email;

    @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    @Schema(description = "Телефон клиента", example = "+79991234567", required = true)
    private String phone;

    @Schema(description = "Является ли клиент постоянным", example = "false")
    private Boolean isPermanent = false;
}