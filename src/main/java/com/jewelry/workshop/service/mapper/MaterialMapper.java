package com.jewelry.workshop.service.mapper;

import com.jewelry.workshop.domain.model.dto.material.MaterialResponseDTO;
import com.jewelry.workshop.domain.model.entity.Material;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class MaterialMapper {
    public MaterialResponseDTO toDto(Material material) {
        if (material == null) return null;
        return new MaterialResponseDTO() {{
            setId(material.getId());
            setName(material.getName());
            setDescription(material.getDescription());
            setCreatedAt(toLocalDateTime(material.getCreatedAt()));
            setUpdatedAt(toLocalDateTime(material.getUpdatedAt()));
        }};
    }

    private LocalDateTime toLocalDateTime(java.time.Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }
}