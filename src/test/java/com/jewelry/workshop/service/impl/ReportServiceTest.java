package com.jewelry.workshop.service.impl;

import com.jewelry.workshop.domain.model.entity.Product;
import com.jewelry.workshop.domain.repository.*;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportServiceTest {
    @Test void productsWithoutSalesReturnZeroInsteadOfNullPointer() {
        ProductRepository products = mock(ProductRepository.class);
        Product p = new Product(); p.setId(1L); p.setName("Ring");
        when(products.getProductSalesStatistics(any(), any(), any()))
                .thenReturn(Collections.singletonList(new Object[]{p, 0L, null, null}));
        var service = new ReportServiceImpl(mock(OrderRepository.class), mock(OrderItemRepository.class), mock(ClientRepository.class), products);
        var result = service.getProductReport(Instant.EPOCH, Instant.now()).get(0);
        assertEquals(0L, result.getTotalSold()); assertEquals(BigDecimal.ZERO, result.getTotalRevenue());
    }
    @Test void invertedPeriodIsRejectedBeforeQuerying() {
        OrderRepository orders = mock(OrderRepository.class);
        var service = new ReportServiceImpl(orders, mock(OrderItemRepository.class), mock(ClientRepository.class), mock(ProductRepository.class));
        assertThrows(IllegalArgumentException.class, () -> service.getSalesReport(Instant.now(), Instant.EPOCH));
        verifyNoInteractions(orders);
    }
}
