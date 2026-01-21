package com.jewelry.workshop.domain.model.dto.orderitem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO для создания позиции в заказе")
public class OrderItemCreateDTO {

    @NotNull(message = "ID товара обязателен")
    @Schema(description = "ID товара", example = "1", required = true)
    private Long productId;

    @NotNull(message = "Количество обязательно")
    @Min(value = 1, message = "Количество должно быть не менее 1")
    @Schema(description = "Количество товара", example = "2", required = true)
    private Integer quantity;
}