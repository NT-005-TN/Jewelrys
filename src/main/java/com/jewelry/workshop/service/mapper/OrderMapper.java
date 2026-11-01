package com.jewelry.workshop.service.mapper;

import com.jewelry.workshop.domain.model.dto.order.OrderResponseDTO;
import com.jewelry.workshop.domain.model.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final ClientMapper clientMapper;
    private final OrderItemMapper orderItemMapper;

    public OrderResponseDTO toDto(Order order){
        if(order == null) return null;

        return OrderResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .client(clientMapper.toDto(order.getClient()))
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .notes(order.getNotes())
                .items(order.getOrderItems().stream().map(orderItemMapper::toDto).toList())
                .orderDatetime(toLocalDateTime(order.getOrderDatetime()))
                .completedAt(toLocalDateTime(order.getCompletedAt()))
                .createdAt(toLocalDateTime(order.getCreatedAt()))
                .updatedAt(toLocalDateTime(order.getUpdatedAt()))
                .build();
    }
    private LocalDateTime toLocalDateTime(java.time.Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }
}