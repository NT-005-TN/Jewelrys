package com.jewelry.workshop.domain.model.dto.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(builderMethodName = "builder", access = AccessLevel.PUBLIC)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO для ответа с информацией об изделии")
public class ProductResponseDTO {
    @Schema(description = "ID изделия", example = "1")
    private Long id;
    @Schema(description = "Название изделия", example = "Золотое кольцо с бриллиантом")
    private String name;
    @Schema(description = "Описание изделия", example = "Элегантное кольцо из желтого золота")
    private String description;
    @Schema(description = "Артикул (SKU)", example = "RING-GD-001")
    private String sku;
    @Schema(description = "Вес изделия в граммах", example = "5.250")
    private BigDecimal weight;
    @Schema(description = "Цена изделия", example = "45000.00")
    private BigDecimal price;
    @Schema(description = "Тип изделия", example = "КОЛЬЦО")
    private String type;
    @Schema(description = "Количество на складе", example = "10")
    private Integer inStock;
    @Schema(description = "Доступно ли изделие для заказа", example = "true")
    private Boolean isAvailable;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата создания", example = "2024-01-15 14:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата последнего обновления", example = "2024-01-15 14:30:00")
    private LocalDateTime updatedAt;
}