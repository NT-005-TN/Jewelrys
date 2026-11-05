package com.jewelry.workshop.service.impl;

import com.jewelry.workshop.domain.model.dto.report.ClientReportDTO;
import com.jewelry.workshop.domain.model.dto.report.ProductReportItemDTO;
import com.jewelry.workshop.domain.model.dto.report.SalesReportDTO;
import com.jewelry.workshop.domain.model.entity.Client;
import com.jewelry.workshop.domain.model.entity.Order;
import com.jewelry.workshop.domain.model.entity.Product;
import com.jewelry.workshop.domain.repository.*;
import com.jewelry.workshop.service.interfaces.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@org.springframework.transaction.annotation.Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    private void validatePeriod(Instant start, Instant end) {
        if (start == null || end == null || start.isAfter(end)) {
            throw new IllegalArgumentException("Начало периода должно быть не позднее конца");
        }
    }

    @Override
    public SalesReportDTO getSalesReport(Instant start, Instant end) {
        validatePeriod(start, end);
        LocalDateTime startDate = LocalDateTime.ofInstant(start, java.time.ZoneOffset.UTC);
        LocalDateTime endDate = LocalDateTime.ofInstant(end, java.time.ZoneOffset.UTC);

        List<Object[]> stats = orderRepository.getClientStatisticsInPeriod(start, end);
        long totalOrders = stats.stream().mapToLong(row -> ((Number) row[3]).longValue()).sum();

        BigDecimal totalRevenue = stats.stream()
                .map(row -> (BigDecimal) row[4])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long uniqueClients = stats.size();

        String mostPopularProduct = "Не определён";
        String mostActiveClient = "Не определён";

        var bestSellingPage = orderItemRepository.findBestSellingProductsInPeriod(start, end, PageRequest.of(0, 1));
        if (!bestSellingPage.isEmpty()) {
            Object[] first = bestSellingPage.getContent().get(0);
            Long productId = ((Number) first[0]).longValue();
            var productOpt = productRepository.findById(productId);
            mostPopularProduct = productOpt.map(p -> p.getName() + " (SKU: " + p.getSku() + ")").orElse("N/A");
        }

        if (!stats.isEmpty()) {
            Object[] top = stats.get(0);
            String lastName = (String) top[1];
            String firstName = (String) top[2];
            mostActiveClient = lastName + " " + firstName;
        }

        BigDecimal avgOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalDiscounts = orderRepository.sumDiscounts(start, end);

        String finalMostPopularProduct = mostPopularProduct;
        String finalMostActiveClient = mostActiveClient;
        return new SalesReportDTO() {{
            setPeriodStart(startDate);
            setPeriodEnd(endDate);
            setTotalOrders(totalOrders);
            setTotalRevenue(totalRevenue);
            setAverageOrderValue(avgOrderValue);
            setUniqueClients(uniqueClients);
            setMostPopularProduct(finalMostPopularProduct);
            setMostActiveClient(finalMostActiveClient);
            setTotalDiscounts(totalDiscounts);
        }};
    }

    @Override
    public ClientReportDTO getClientReport(Instant start, Instant end) {
        validatePeriod(start, end);
        long totalClients = clientRepository.count();
        long permanentClients = clientRepository.countPermanentClients();
        long newClients = clientRepository.countByCreatedAtBetween(start, end);
        var stats = orderRepository.getClientStatisticsInPeriod(start, end);
        long periodOrders = stats.stream().mapToLong(row -> ((Number) row[3]).longValue()).sum();
        double avgOrdersPerClient = totalClients == 0 ? 0 : (double) periodOrders / totalClients;
        BigDecimal averageLTV = totalClients == 0 ? BigDecimal.ZERO : orderRepository.lifetimeRevenue()
                .divide(BigDecimal.valueOf(totalClients), 2, java.math.RoundingMode.HALF_UP);
        String topClientByRevenue = stats.isEmpty() ? "Нет данных" : stats.get(0)[1] + " " + stats.get(0)[2];
        String topClientByOrders = stats.stream().max(java.util.Comparator.comparingLong(row -> ((Number) row[3]).longValue()))
                .map(row -> row[1] + " " + row[2]).orElse("Нет данных");

        String finalTopClientByOrders = topClientByOrders;
        String finalTopClientByRevenue = topClientByRevenue;
        return new ClientReportDTO() {{
            setTotalClients(totalClients);
            setPermanentClients(permanentClients);
            setNewClients(newClients);
            setAverageOrdersPerClient(avgOrdersPerClient);
            setAverageLTV(averageLTV);
            setTopClientByRevenue(finalTopClientByRevenue);
            setTopClientByOrders(finalTopClientByOrders);
        }};
    }

    @Override
    public List<ProductReportItemDTO> getProductReport(Instant start, Instant end) {
        validatePeriod(start, end);
        var stats = productRepository.getProductSalesStatistics(start, end, PageRequest.of(0, 100));

        return stats.stream().map(row -> {
            Product product = (Product) row[0];
            Long orderCount = ((Number) row[1]).longValue();
            Long totalSold = row[2] == null ? 0L : ((Number) row[2]).longValue();
            BigDecimal totalRevenue = row[3] == null ? BigDecimal.ZERO : (BigDecimal) row[3];

            ProductReportItemDTO dto = new ProductReportItemDTO();
            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
            dto.setSku(product.getSku());
            dto.setOrderCount(orderCount);
            dto.setTotalSold(totalSold);
            dto.setTotalRevenue(totalRevenue);
            dto.setInStock(product.getInStock());

            return dto;
        }).toList();
    }
}