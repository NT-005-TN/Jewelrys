package com.jewelry.workshop.domain.model.dto.material;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO для создания материала")
public class MaterialCreateDTO {

    @NotBlank(message = "Название материала обязательно")
    @Size(max = 50, message = "Название не должно превышать 50 символов")
    @Schema(description = "Название материала", example = "Золото 585", required = true)
    private String name;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    @Schema(description = "Описание материала", example = "Желтое золото 585 пробы")
    private String description;
}