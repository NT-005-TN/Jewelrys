package com.jewelry.workshop.domain.model.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Ответ на поиск изделий с пагинацией")
public class ProductSearchResponseDTO {
    @Schema(description = "Список найденных изделий")
    private List<ProductResponseDTO> items;

    @Schema(description = "Номер текущей страницы (начиная с 0)", example = "0")
    private int page;

    @Schema(description = "Размер страницы", example = "10")
    private int size;

    @Schema(description = "Общее количество найденных изделий", example = "25")
    private long totalElements;

    @Schema(description = "Общее количество страниц", example = "3")
    private int totalPages;

    @Schema(description = "Это первая страница?", example = "true")
    private boolean first;

    @Schema(description = "Это последняя страница?", example = "false")
    private boolean last;

    @Schema(description = "Результаты отсутствуют?", example = "false")
    private boolean empty;
}