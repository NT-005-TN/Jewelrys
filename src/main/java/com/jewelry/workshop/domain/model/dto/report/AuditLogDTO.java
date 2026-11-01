package com.jewelry.workshop.domain.model.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Запись системного аудита")
public record AuditLogDTO(

        @Schema(description = "Уникальный идентификатор записи", example = "123")
        Long id,

        @Schema(description = "Имя пользователя, выполнившего действие", example = "admin")
        String username,

        @Schema(description = "Тип действия", allowableValues = {"CREATE", "UPDATE", "DELETE"}, example = "UPDATE")
        String action,

        @Schema(description = "Название таблицы, в которой произошло изменение", example = "orders")
        String tableName,

        @Schema(description = "ID изменённой записи в таблице", example = "456")
        Long recordId,

        @Schema(description = "IP-адрес клиента", example = "192.168.1.100")
        String ipAddress,

        @Schema(description = "Дата и время создания записи", example = "2026-01-17T18:30:00Z")
        Instant createdAt

) {}