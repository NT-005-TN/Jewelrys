package com.jewelry.workshop.presentation.controller;

import com.jewelry.workshop.presentation.exception.error.ErrorResponseDTO;
import com.jewelry.workshop.service.interfaces.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "Администрирование", description = "Системные операции (только ADMIN)")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Системная статистика")
    @ApiResponse(responseCode = "200", description = "Статистика успешно получена",
            content = @Content(schema = @Schema(implementation = Map.class))) // можно создать DTO, но пока Map
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Просмотр системных логов")
    @ApiResponse(responseCode = "200", description = "Логи успешно получены",
            content = @Content(schema = @Schema(implementation = Page.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<Page<?>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return ResponseEntity.ok(adminService.getAuditLogs(pageable));
    }

}