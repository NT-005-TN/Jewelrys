package com.jewelry.workshop.service.mapper;

import com.jewelry.workshop.domain.model.dto.orderitem.OrderItemResponseDTO;
import com.jewelry.workshop.domain.model.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderItemMapper {
    public OrderItemResponseDTO toDto(OrderItem item) {
        if (item == null) return null;
        return new OrderItemResponseDTO() {{
            setId(item.getId());
            setProductId(item.getProduct().getId());
            setProductName(item.getProduct().getName());
            setProductSku(item.getProduct().getSku());
            setQuantity(item.getQuantity());
            setUnitPrice(item.getUnitPrice());
            setTotalPrice(item.getTotalPrice());
            setUnitWeight(item.getProduct().getWeight());
            setTotalWeight(item.getProduct().getWeight().multiply(BigDecimal.valueOf(item.getQuantity())));
        }};
    }
}
