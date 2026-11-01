package com.jewelry.workshop.domain.model.dto.orderitem;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO для ответа с информацией о позиции в заказе")
public class OrderItemResponseDTO {

    @Schema(description = "ID позиции заказа", example = "1")
    private Long id;

    @Schema(description = "ID товара", example = "1")
    private Long productId;

    @Schema(description = "Название товара", example = "Золотое кольцо с бриллиантом")
    private String productName;

    @Schema(description = "Артикул товара", example = "RING-GD-001")
    private String productSku;

    @Schema(description = "Количество", example = "2")
    private Integer quantity;

    @Schema(description = "Цена за единицу", example = "22500.00")
    private BigDecimal unitPrice;

    @Schema(description = "Общая цена позиции", example = "45000.00")
    private BigDecimal totalPrice;

    @Schema(description = "Вес за единицу (г)", example = "5.250")
    private BigDecimal unitWeight;

    @Schema(description = "Общий вес позиции (г)", example = "10.500")
    private BigDecimal totalWeight;
}