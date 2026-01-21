package com.jewelry.workshop.domain.model.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jewelry.workshop.domain.model.dto.client.ClientResponseDTO;
import com.jewelry.workshop.domain.model.dto.orderitem.OrderItemResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(builderMethodName = "builder", access = AccessLevel.PUBLIC)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO для ответа с информацией о заказе")
public class OrderResponseDTO {

    @Schema(description = "ID заказа", example = "1")
    private Long id;

    @Schema(description = "Номер заказа", example = "1000001")
    private Long orderNumber;

    @Schema(description = "Информация о клиенте")
    private ClientResponseDTO client;

    @Schema(description = "Статус заказа", example = "PROCESSING")
    private String status;

    @Schema(description = "Общая сумма заказа", example = "45000.00")
    private BigDecimal totalAmount;

    @Schema(description = "Сумма скидки", example = "4500.00")
    private BigDecimal discountAmount;

    @Schema(description = "Итоговая сумма к оплате", example = "40500.00")
    private BigDecimal finalAmount;

    @Schema(description = "Примечания к заказу", example = "Нужно доставить к 25 декабря")
    private String notes;

    @Schema(description = "Список товаров в заказе")
    private List<OrderItemResponseDTO> items;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата и время заказа", example = "2024-01-15 14:30:00")
    private LocalDateTime orderDatetime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата завершения", example = "2024-01-20 14:30:00")
    private LocalDateTime completedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата создания", example = "2024-01-15 14:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата последнего обновления", example = "2024-01-15 14:30:00")
    private LocalDateTime updatedAt;
}