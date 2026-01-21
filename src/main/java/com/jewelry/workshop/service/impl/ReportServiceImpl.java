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

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    @Override
    public SalesReportDTO getSalesReport(Instant start, Instant end) {
        LocalDateTime startDate = LocalDateTime.ofInstant(start, ZoneId.systemDefault());
        LocalDateTime endDate = LocalDateTime.ofInstant(end, ZoneId.systemDefault());

        List<Object[]> stats = orderRepository.getClientStatisticsInPeriod(start, end);
        long totalOrders = orderRepository.findOrdersByCriteria(
                null, null, start, end, null, null, PageRequest.of(0, 1)
        ).getTotalElements();

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
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalDiscounts = orderRepository.findByOrderDatetimeBetween(startDate, endDate).stream()
                .map(Order::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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
        long totalClients = clientRepository.count();
        long permanentClients = clientRepository.countPermanentClients();
        long newClients = clientRepository.countNewClientsSince(start);

        double avgOrdersPerClient = totalClients > 0
                ? (double) orderRepository.count() / totalClients
                : 0.0;

        BigDecimal totalSpentAll = orderRepository.findAll().stream()
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageLTV = totalClients > 0
                ? totalSpentAll.divide(BigDecimal.valueOf(totalClients), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        String topClientByRevenue = "Нет данных";
        String topClientByOrders = "Нет данных";

        var topClients = clientRepository.getPermanentClientDetails(PageRequest.of(0, 1));
        if (!topClients.isEmpty()) {
            Object[] top = topClients.get(0);
            Client client = (Client) top[0];
            topClientByRevenue = client.getFullName();
        }

        var stats = clientRepository.getClientStatistics(start, end, null);
        if (!stats.isEmpty()) {
            Object[] top = stats.get(0);
            Client client = (Client) top[0];
            topClientByOrders = client.getFullName();
        }

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
        var stats = productRepository.getProductSalesStatistics(start, end, PageRequest.of(0, 100));

        return stats.stream().map(row -> {
            Product product = (Product) row[0];
            Long orderCount = ((Number) row[1]).longValue();
            Long totalSold = ((Number) row[2]).longValue();
            BigDecimal totalRevenue = (BigDecimal) row[3];

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