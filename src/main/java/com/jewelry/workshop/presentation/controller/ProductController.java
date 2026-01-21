package com.jewelry.workshop.presentation.controller;

import com.jewelry.workshop.domain.model.dto.product.*;
import com.jewelry.workshop.presentation.exception.error.ErrorResponseDTO;
import com.jewelry.workshop.security.auth.UserDetailsImpl;
import com.jewelry.workshop.service.interfaces.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/products")
@Tag(name = "Товары (Изделия)", description = "Управление и просмотр ювелирных изделий")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Просмотр каталога изделий", description = "Возвращает постраничный список изделий с фильтрацией и сортировкой. Доступно всем авторизованным пользователям.")
    @ApiResponse(
            responseCode = "200",
            description = "Список изделий успешно получен",
            content = @Content(schema = @Schema(implementation = ProductSearchResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Неавторизованный доступ: токен отсутствует, недействителен или просрочен",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    public ResponseEntity<ProductSearchResponseDTO> getProducts(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minWeight,
            @RequestParam(required = false) BigDecimal maxWeight,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userRole = userDetails.getUserRole();
        ProductSearchDTO search = new ProductSearchDTO();
        search.setName(name);
        search.setType(type);
        search.setMinPrice(minPrice);
        search.setMaxPrice(maxPrice);
        search.setMinWeight(minWeight);
        search.setMaxWeight(maxWeight);
        search.setMinStock(minStock);
        search.setIsAvailable(isAvailable);
        search.setSortBy(sortBy);
        search.setSortDirection(sortDirection);

        ProductSearchResponseDTO response = productService.searchProducts(userRole, search, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить детали изделия по ID", description = "Возвращает полную информацию об изделии. Клиенты видят только доступные изделия.")
    @ApiResponse(
            responseCode = "200",
            description = "Детали изделия успешно получены",
            content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Неавторизованный доступ: токен отсутствует, недействителен или просрочен",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Запрещено: клиент пытается получить недоступное (скрытое/неактивное) изделие",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Изделие с указанным ID не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    public ResponseEntity<ProductResponseDTO> getProductById(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id
    ) {
        String userRole = userDetails.getUserRole();
        ProductResponseDTO product = productService.getProductById(id, userRole);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новое изделие", description = "Только администратор может добавлять новые изделия в каталог.")
    @ApiResponse(
            responseCode = "201",
            description = "Изделие успешно создано",
            content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Некорректные данные запроса (валидация DTO провалена)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Неавторизованный доступ",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещён: только ADMIN может создавать изделия",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductCreateDTO dto) {
        ProductResponseDTO product = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить изделие по ID", description = "Полное или частичное обновление данных изделия. Только для ADMIN.")
    @ApiResponse(
            responseCode = "200",
            description = "Изделие успешно обновлено",
            content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Некорректные данные запроса (валидация DTO провалена)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Неавторизованный доступ",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещён: только ADMIN может редактировать изделия",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Изделие с указанным ID не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateDTO dto
    ) {
        ProductResponseDTO updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить изделие по ID", description = "Логическое или физическое удаление изделия. Только для ADMIN.")
    @ApiResponse(
            responseCode = "200",
            description = "Изделие успешно удалено",
            content = @Content(schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Неавторизованный доступ",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Доступ запрещён: только ADMIN может удалять изделия",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Изделие с указанным ID не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
    )
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Изделие успешно удалено");
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Обновить остаток изделия на складе")
    @ApiResponse(responseCode = "200", description = "Остаток успешно обновлён")
    @ApiResponse(responseCode = "400", description = "Некорректные данные (остаток < 0 или null)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён (требуется роль SELLER или ADMIN)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Изделие не найдено",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<String> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateDTO dto  // ← ОБЯЗАТЕЛЬНО через тело!
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID изделия должен быть положительным числом");
        }
        productService.updateInStock(id, dto.getInStock());
        return ResponseEntity.ok("Остаток изделия #" + id + " обновлён до " + dto.getInStock());
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Получить список изделий с низким остатком")
    @ApiResponse(responseCode = "200", description = "Список успешно получен",
            content = @Content(schema = @Schema(implementation = ProductSearchResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<ProductSearchResponseDTO> getLowStockProducts(
            @RequestParam(defaultValue = "5") Integer threshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ProductSearchResponseDTO response = productService.getLowStockProducts(page, size, threshold);
        return ResponseEntity.ok(response);
    }
}