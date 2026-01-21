package com.jewelry.workshop.domain.model.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO для поиска изделий")
public class ProductSearchDTO {

    @Schema(description = "Название для поиска")
    private String name;

    @Schema(description = "Тип изделия", example = "КОЛЬЦО")
    private String type;

    @Schema(description = "Минимальная цена", example = "1000.00")
    private BigDecimal minPrice;

    @Schema(description = "Максимальная цена", example = "100000.00")
    private BigDecimal maxPrice;

    @Schema(description = "Минимальный вес", example = "1.000")
    private BigDecimal minWeight;

    @Schema(description = "Максимальный вес", example = "50.000")
    private BigDecimal maxWeight;

    @Schema(description = "Минимальное количество на складе", example = "1")
    private Integer minStock;

    @Schema(description = "Доступность", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Сортировка",
            allowableValues = {"name", "price", "weight", "stock", "created"},
            example = "name")
    private String sortBy;

    @Schema(description = "Направление сортировки",
            allowableValues = {"asc", "desc"},
            example = "asc")
    private String sortDirection = "asc";

    @Schema(description = "Только товары со скидкой", example = "false")
    private Boolean hasDiscount = false;
}