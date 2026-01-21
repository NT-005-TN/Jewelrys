package com.jewelry.workshop.service.interfaces;

import com.jewelry.workshop.domain.model.dto.report.ClientReportDTO;
import com.jewelry.workshop.domain.model.dto.report.ProductReportItemDTO;
import com.jewelry.workshop.domain.model.dto.report.SalesReportDTO;
import java.time.Instant;
import java.util.List;

public interface ReportService {
    SalesReportDTO getSalesReport(Instant start, Instant end);
    ClientReportDTO getClientReport(Instant start, Instant end);
    List<ProductReportItemDTO> getProductReport(Instant start, Instant end);
}