package com.jewelry.workshop.service.mapper;

import com.jewelry.workshop.domain.model.dto.product.ProductResponseDTO;
import com.jewelry.workshop.domain.model.entity.Product;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class ProductMapper {
    public ProductResponseDTO toDto(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .type(product.getType())
                .weight(product.getWeight())
                .price(product.getPrice())
                .inStock(product.getInStock())
                .isAvailable(product.getIsAvailable()) // ← добавлено
                .createdAt(product.getCreatedAt() != null ?
                        LocalDateTime.ofInstant(product.getCreatedAt(), ZoneId.systemDefault()) : null)
                .updatedAt(product.getUpdatedAt() != null ?
                        LocalDateTime.ofInstant(product.getUpdatedAt(), ZoneId.systemDefault()) : null)
                .build();
    }
}