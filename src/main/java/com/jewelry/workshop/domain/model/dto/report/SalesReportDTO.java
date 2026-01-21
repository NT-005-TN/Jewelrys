package com.jewelry.workshop.domain.model.dto.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "DTO для отчета по продажам")
public class SalesReportDTO {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата начала периода")
    private LocalDateTime periodStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Дата окончания периода")
    private LocalDateTime periodEnd;

    @Schema(description = "Общее количество заказов", example = "150")
    private Long totalOrders;

    @Schema(description = "Общая выручка", example = "1250000.00")
    private BigDecimal totalRevenue;

    @Schema(description = "Средний чек", example = "8333.33")
    private BigDecimal averageOrderValue;

    @Schema(description = "Количество уникальных клиентов", example = "75")
    private Long uniqueClients;

    @Schema(description = "Самое популярное изделие")
    private String mostPopularProduct;

    @Schema(description = "Самый активный клиент")
    private String mostActiveClient;

    @Schema(description = "Общая сумма скидок", example = "125000.00")
    private BigDecimal totalDiscounts;
}