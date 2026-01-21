package com.jewelry.workshop.service.interfaces;

import com.jewelry.workshop.domain.model.dto.order.OrderCreateDTO;
import com.jewelry.workshop.domain.model.dto.order.OrderResponseDTO;
import com.jewelry.workshop.domain.model.dto.report.DailyOrderStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    Page<OrderResponseDTO> getMyOrders(Long clientId, int page, int size);
    Page<OrderResponseDTO> getOrderHistory(Long clientId, int page, int size);

    OrderResponseDTO createOrder(Long clientId, OrderCreateDTO dto);
    OrderResponseDTO getOrderById(Long orderId, Long clientId);

    void cancelOrder(Long orderId, Long clientId);
    Page<OrderResponseDTO> getAllOrders(Pageable pageable);

    void changeStatus(Long orderId, String status, Long currentUserId);
    List<DailyOrderStatsDTO> getDailyOrderStats(Integer days);
}
