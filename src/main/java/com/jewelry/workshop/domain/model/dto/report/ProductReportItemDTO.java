package com.jewelry.workshop.domain.model.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO для отчета по изделиям (продуктам)")
public class ProductReportItemDTO {

    @Schema(description = "ID изделия", example = "123")
    private Long productId;

    @Schema(description = "Название изделия", example = "Кольцо 'Звезда'")
    private String productName;

    @Schema(description = "Артикул (SKU)", example = "SKU-789")
    private String sku;

    @Schema(description = "Количество заказов с этим изделием", example = "42")
    private Long orderCount;

    @Schema(description = "Общее количество проданных единиц", example = "65")
    private Long totalSold;

    @Schema(description = "Общая выручка от изделия", example = "130000.00")
    private BigDecimal totalRevenue;

    @Schema(description = "Остаток на складе", example = "10")
    private Integer inStock;
}