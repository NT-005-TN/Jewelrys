package com.jewelry.workshop.domain.model.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO для обновления клиента")
public class ClientUpdateDTO {

    @Size(min = 2, max = 50, message = "Имя должно быть от 2 до 50 символов")
    @Schema(description = "Имя клиента", example = "Иван")
    private String firstName;

    @Size(min = 2, max = 50, message = "Фамилия должна быть от 2 до 50 символов")
    @Schema(description = "Фамилия клиента", example = "Петров")
    private String lastName;

    @Size(max = 50, message = "Отчество не должно превышать 50 символов")
    @Schema(description = "Отчество клиента", example = "Сергеевич")
    private String patronymic;

    @Email(message = "Некорректный формат email")
    @Schema(description = "Email клиента", example = "ivan.petrov@example.com")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Некорректный формат номера телефона")
    @Schema(description = "Телефон клиента", example = "+79991234567")
    private String phone;

}