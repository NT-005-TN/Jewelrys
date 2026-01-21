package com.jewelry.workshop.domain.model.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO для отчета по клиентам")
public class ClientReportDTO {

    @Schema(description = "Общее количество клиентов", example = "200")
    private Long totalClients;

    @Schema(description = "Количество постоянных клиентов", example = "50")
    private Long permanentClients;

    @Schema(description = "Количество новых клиентов за период", example = "25")
    private Long newClients;

    @Schema(description = "Среднее количество заказов на клиента", example = "3.5")
    private Double averageOrdersPerClient;

    @Schema(description = "Средний LTV (Lifetime Value)", example = "45000.00")
    private BigDecimal averageLTV;

    @Schema(description = "Топ клиент по выручке")
    private String topClientByRevenue;

    @Schema(description = "Топ клиент по количеству заказов")
    private String topClientByOrders;
}