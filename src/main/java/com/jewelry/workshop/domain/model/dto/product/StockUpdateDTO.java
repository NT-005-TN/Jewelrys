package com.jewelry.workshop.domain.model.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO для обновления остатка изделия на складе")
public class StockUpdateDTO {

    @NotNull(message = "Остаток обязателен")
    @Min(value = 0, message = "Остаток не может быть отрицательным")
    @Schema(
            description = "Новое количество изделия на складе",
            example = "15",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer inStock;
}