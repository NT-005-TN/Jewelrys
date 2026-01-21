package com.jewelry.workshop.domain.model.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO для обновления изделия")
public class ProductUpdateDTO {

    @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
    @Schema(description = "Название изделия", example = "Золотое кольцо с бриллиантом")
    private String name;

    @Schema(description = "Описание изделия", example = "Элегантное кольцо из желтого золота с бриллиантом 0.5 карат")
    private String description;

    @DecimalMin(value = "0.001", message = "Вес должен быть больше 0")
    @Schema(description = "Вес изделия в граммах", example = "5.250")
    private BigDecimal weight;

    @DecimalMin(value = "0.00", message = "Цена не может быть отрицательной")
    @Schema(description = "Цена изделия", example = "45000.00")
    private BigDecimal price;

    @Schema(description = "Тип изделия", example = "КОЛЬЦО")
    private String type;

    @Min(value = 0, message = "Количество не может быть отрицательным")
    @Schema(description = "Количество на складе", example = "10")
    private Integer inStock;

    @Schema(description = "Доступно ли изделие для заказа", example = "true")
    private Boolean isAvailable;
}