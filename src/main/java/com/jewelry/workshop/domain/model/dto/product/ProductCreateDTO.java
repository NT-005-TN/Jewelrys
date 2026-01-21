package com.jewelry.workshop.domain.model.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO для создания изделия")
public class ProductCreateDTO {

    @NotBlank(message = "Название изделия обязательно")
    @Size(min = 2, max = 100, message = "Название должно быть от 2 до 100 символов")
    @Schema(description = "Название изделия", example = "Золотое кольцо с бриллиантом", required = true)
    private String name;

    @Schema(description = "Описание изделия", example = "Элегантное кольцо из желтого золота")
    private String description;

    @NotNull(message = "Вес обязателен")
    @DecimalMin(value = "0.001", message = "Вес должен быть больше 0")
    @Schema(description = "Вес изделия в граммах", example = "5.250", required = true)
    private BigDecimal weight;

    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.00", message = "Цена не может быть отрицательной")
    @Schema(description = "Цена изделия", example = "45000.00", required = true)
    private BigDecimal price;

    @NotBlank(message = "Тип изделия обязателен")
    @Pattern(regexp = "КОЛЬЦО|СЕРЬГИ|БРАСЛЕТ|КОЛЬЕ|ПОДВЕСКА|ЧАСЫ|БРОШЬ|ЗАПОНКИ",
            message = "Недопустимый тип изделия. Допустимые значения: КОЛЬЦО, СЕРЬГИ, БРАСЛЕТ, КОЛЬЕ, ПОДВЕСКА, ЧАСЫ, БРОШЬ, ЗАПОНКИ")
    @Schema(description = "Тип изделия", example = "КОЛЬЦО", required = true,
            allowableValues = {"КОЛЬЦО", "СЕРЬГИ", "БРАСЛЕТ", "КОЛЬЕ", "ПОДВЕСКА", "ЧАСЫ", "БРОШЬ", "ЗАПОНКИ"})
    private String type;

    @Schema(description = "Артикул (SKU)", example = "RING-GD-001")
    @NotBlank(message = "Артикул (SKU) обязателен и должен быть уникален")
    @Pattern(regexp = "^[A-Z0-9\\-_]+$", message = "Артикул может содержать только заглавные буквы, цифры, дефис и подчёркивание")
    private String sku;

    @NotNull(message = "Количество на складе обязательно")
    @Min(value = 0, message = "Количество не может быть отрицательным")
    @Schema(description = "Количество на складе", example = "10", required = true)
    private Integer inStock;

    @Schema(description = "Минимальный порог остатка", example = "5")
    private Integer minStockThreshold = 5;
}