package com.jewelry.workshop.domain.model.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Ежедневная статистика по заказам")
public class DailyOrderStatsDTO {
    private LocalDate date;
    private Long orderCount;
    private BigDecimal dailyRevenue;
    private BigDecimal avgOrderValue;
}