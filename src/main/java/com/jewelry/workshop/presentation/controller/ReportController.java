package com.jewelry.workshop.presentation.controller;

import com.jewelry.workshop.domain.model.dto.report.ClientReportDTO;
import com.jewelry.workshop.domain.model.dto.report.SalesReportDTO;
import com.jewelry.workshop.presentation.exception.error.ErrorResponseDTO;
import com.jewelry.workshop.service.interfaces.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@Tag(name = "Отчеты", description = "Аналитические отчеты (только ADMIN)")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Детальный отчет по продажам")
    @ApiResponse(responseCode = "200", description = "Отчет успешно сформирован",
            content = @Content(schema = @Schema(implementation = SalesReportDTO.class)))
    @ApiResponse(responseCode = "400", description = "Некорректные даты",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<SalesReportDTO> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        return ResponseEntity.ok(reportService.getSalesReport(start, end));
    }

    @GetMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Отчет по клиентам")
    @ApiResponse(responseCode = "200", description = "Отчет успешно сформирован",
            content = @Content(schema = @Schema(implementation = ClientReportDTO.class)))
    @ApiResponse(responseCode = "400", description = "Некорректные даты",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<ClientReportDTO> getClientReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        return ResponseEntity.ok(reportService.getClientReport(start, end));
    }

    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Отчет по изделиям")
    @ApiResponse(responseCode = "200", description = "Отчет успешно сформирован",
            content = @Content(schema = @Schema(implementation = Object.class))) // лучше создать DTO
    @ApiResponse(responseCode = "400", description = "Некорректные даты",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Доступ запрещён",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    public ResponseEntity<List<Map<String, Object>>> getProductReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        Object report = reportService.getProductReport(start, end);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> typedReport = (List<Map<String, Object>>) report;
        return ResponseEntity.ok(typedReport);
    }
}