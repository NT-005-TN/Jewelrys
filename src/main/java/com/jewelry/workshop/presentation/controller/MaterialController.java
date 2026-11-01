package com.jewelry.workshop.presentation.controller;

import com.jewelry.workshop.domain.model.dto.material.MaterialCreateDTO;
import com.jewelry.workshop.domain.model.dto.material.MaterialResponseDTO;
import com.jewelry.workshop.domain.model.dto.material.MaterialUpdateDTO;
import com.jewelry.workshop.presentation.exception.error.ErrorResponseDTO;
import com.jewelry.workshop.service.interfaces.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/materials")
@Tag(name = "Материалы", description = "Управление материалами (только ADMIN)")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить список всех материалов")
    public ResponseEntity<List<MaterialResponseDTO>> getAllMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MaterialResponseDTO> pageResult = materialService.getAllMaterials(PageRequest.of(page, size));
        return ResponseEntity.ok(pageResult.getContent());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новый материал")
    @ApiResponse(responseCode = "201", description = "Материал успешно создан",
            content = @Content(schema = @Schema(implementation = MaterialResponseDTO.class)))
    public ResponseEntity<MaterialResponseDTO> createMaterial(@Valid @RequestBody MaterialCreateDTO dto) {
        MaterialResponseDTO created = materialService.createMaterial(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить материал")
    @ApiResponse(responseCode = "200", description = "Материал успешно обновлён",
            content = @Content(schema = @Schema(implementation = MaterialResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Материал не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<MaterialResponseDTO> updateMaterial(
            @PathVariable Long id,
            @Valid @RequestBody MaterialUpdateDTO dto) {
        MaterialResponseDTO updated = materialService.updateMaterial(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить материал")
    @ApiResponse(responseCode = "200", description = "Материал успешно удалён")
    @ApiResponse(responseCode = "400", description = "Нельзя удалить материал, используемый в изделиях",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Материал не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<String> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.ok("Материал успешно удалён");
    }


}