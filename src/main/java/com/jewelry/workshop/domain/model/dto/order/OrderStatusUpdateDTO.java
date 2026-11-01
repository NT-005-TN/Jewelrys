package com.jewelry.workshop.domain.model.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO для обновления статуса заказа")
public class OrderStatusUpdateDTO {

    @NotNull(message = "Статус обязателен")
    @Schema(description = "Новый статус заказа", example = "PROCESSING", required = true,
            allowableValues = {"PENDING", "PROCESSING", "COMPLETED", "CANCELLED", "DELIVERED"})
    private String status;

    @Schema(description = "Комментарий к изменению статуса", example = "Заказ передан в производство")
    @Size(max = 500, message = "Комментарий не должен превышать 500 символов")
    private String comment;
}
