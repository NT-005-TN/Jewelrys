package com.jewelry.workshop.domain.model.dto.material;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO для ответа с информацией о материале")
public class MaterialResponseDTO {

    @Schema(description = "ID материала", example = "1")
    private Long id;

    @Schema(description = "Название материала", example = "Золото 585")
    private String name;

    @Schema(description = "Описание материала", example = "Желтое золото 585 пробы")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата создания", example = "2024-01-15 14:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата обновления", example = "2024-01-15 14:30:00")
    private LocalDateTime updatedAt;
}