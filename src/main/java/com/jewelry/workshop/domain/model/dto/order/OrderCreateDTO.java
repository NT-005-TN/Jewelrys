package com.jewelry.workshop.domain.model.dto.order;

import com.jewelry.workshop.domain.model.dto.orderitem.OrderItemCreateDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "DTO для создания заказа")
public class OrderCreateDTO {

    @NotEmpty(message = "Заказ должен содержать хотя бы один товар")
    @Schema(description = "Список товаров в заказе", required = true)
    private List<@Valid OrderItemCreateDTO> items;

    @Schema(description = "Примечания к заказу", example = "Нужно доставить к 25 декабря")
    private String notes;
}