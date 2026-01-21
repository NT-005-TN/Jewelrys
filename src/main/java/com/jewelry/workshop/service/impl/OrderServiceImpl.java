package com.jewelry.workshop.service.impl;

import com.jewelry.workshop.domain.model.dto.order.OrderCreateDTO;
import com.jewelry.workshop.domain.model.dto.order.OrderResponseDTO;
import com.jewelry.workshop.domain.model.dto.report.DailyOrderStatsDTO;
import com.jewelry.workshop.domain.model.entity.*;
import com.jewelry.workshop.domain.repository.*;
import com.jewelry.workshop.presentation.exception.UnauthorizedAccessException;
import com.jewelry.workshop.service.interfaces.DiscountService;
import com.jewelry.workshop.service.interfaces.OrderService;
import com.jewelry.workshop.service.mapper.OrderMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final UserRepository userRepository;
    private final DiscountService discountService;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getMyOrders(Long clientId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByClientIdAndStatusNot(
                clientId, Order.STATUS_CANCELLED, pageable
        );
        return orders.map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getOrderHistory(Long clientId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByClientId(clientId, pageable);
        return orders.map(orderMapper::toDto);
    }

    @Override
    @Transactional
    public OrderResponseDTO createOrder(Long clientId, OrderCreateDTO dto) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new UnauthorizedAccessException("Клиент не найден"));

        Set<OrderItem> items = new HashSet<>();
        for (var itemDto : dto.getItems()) {
            // Атомарно пытаемся уменьшить остаток
            int updated = productRepository.decreaseStockIfAvailable(itemDto.getProductId(), itemDto.getQuantity());
            if (updated == 0) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new RuntimeException("Товар с ID " + itemDto.getProductId() + " не найден"));
                throw new RuntimeException(
                        "Недостаточно товара \"" + product.getName() + "\" на складе. Доступно: " +
                                product.getInStock() + ", запрошено: " + itemDto.getQuantity()
                );
            }

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Товар с ID " + itemDto.getProductId() + " не найден"));

            if (!product.isAvailableForSale()) {
                throw new RuntimeException("Товар \"" + product.getName() + "\" недоступен для заказа");
            }

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            items.add(item);
        }

        Order order = new Order();
        order.setClient(client);
        order.setNotes(dto.getNotes());
        order.setStatus(Order.STATUS_PENDING);
        order.setOrderItems(items);

        for (OrderItem item : items) {
            item.setOrder(order);
        }

        order.recalculateTotal();

        BigDecimal discount = discountService.calculateDiscount(order.getTotalAmount(), client);
        order.setDiscountAmount(discount);

        order.calculateTotals();

        Order saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long orderId, Long requestingClientId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        if (requestingClientId != null && !order.getClient().getId().equals(requestingClientId)) {
            throw new UnauthorizedAccessException("Доступ запрещён");
        }

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long clientId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        if (!order.getClient().getId().equals(clientId)) {
            throw new UnauthorizedAccessException("Доступ запрещён");
        }
        if (!order.canBeCancelled()) {
            throw new RuntimeException("Невозможно отменить заказ со статусом: " + order.getStatus());
        }
        order.cancel();

        for (OrderItem item : order.getOrderItems()) {
            item.getProduct().increaseStock(item.getQuantity());
        }

        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        List<OrderResponseDTO> dtos = orders.getContent().stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, orders.getTotalElements());
    }

    @Override
    @Transactional
    public void changeStatus(Long orderId, String newStatus, Long currentUserId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("ID заказа должен быть положительным числом");
        }
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Статус не может быть пустым");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ с ID " + orderId + " не найден"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        if (!User.Role.SELLER.equals(currentUser.getRole()) &&
                !User.Role.ADMIN.equals(currentUser.getRole())) {
            throw new UnauthorizedAccessException("Только продавец или администратор может изменять статус заказа");
        }

        if (!Order.isValidStatus(newStatus)) {
            throw new IllegalArgumentException("Недопустимый статус: " + newStatus);
        }

        if (!isAllowedTransition(order.getStatus(), newStatus)) {
            throw new IllegalStateException(
                    "Невозможно изменить статус с '" + order.getStatus() + "' на '" + newStatus + "'");
        }

        order.updateStatus(newStatus);

        if (Order.STATUS_CANCELLED.equals(newStatus)) {
            for (OrderItem item : order.getOrderItems()) {
                item.getProduct().increaseStock(item.getQuantity());
            }
        }

        orderRepository.save(order);
    }

    private boolean isAllowedTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || newStatus == null) return false;

        if (Order.STATUS_CANCELLED.equals(currentStatus)) {
            return false;
        }

        if (Order.STATUS_COMPLETED.equals(currentStatus) || Order.STATUS_DELIVERED.equals(currentStatus)) {
            return Order.STATUS_CANCELLED.equals(newStatus);
        }

        if (Order.STATUS_PENDING.equals(currentStatus)) {
            return List.of(Order.STATUS_PROCESSING, Order.STATUS_COMPLETED, Order.STATUS_CANCELLED)
                    .contains(newStatus);
        }

        if (Order.STATUS_PROCESSING.equals(currentStatus)) {
            return List.of(Order.STATUS_COMPLETED, Order.STATUS_CANCELLED).contains(newStatus);
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyOrderStatsDTO> getDailyOrderStats(Integer days) {
        int actualDays = (days != null && days > 0) ? days : 7;
        Instant startDate = Instant.now().minus(java.time.Duration.ofDays(actualDays));

        List<Object[]> results = orderRepository.getDailyRevenue(startDate);

        return results.stream().map(row -> {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            LocalDate localDate = sqlDate.toLocalDate();

            Long orderCount = ((Number) row[1]).longValue();
            BigDecimal dailyRevenue = safeToBigDecimal(row[2]);
            BigDecimal avgOrderValue = safeToBigDecimal(row[3]);

            DailyOrderStatsDTO dto = new DailyOrderStatsDTO();
            dto.setDate(localDate);
            dto.setOrderCount(orderCount);
            dto.setDailyRevenue(dailyRevenue != null ? dailyRevenue : BigDecimal.ZERO);
            dto.setAvgOrderValue(avgOrderValue != null ? avgOrderValue : BigDecimal.ZERO);
            return dto;
        }).toList();
    }

    private BigDecimal safeToBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Double d) return BigDecimal.valueOf(d);
        if (value instanceof Float f) return BigDecimal.valueOf(f);
        if (value instanceof Integer i) return BigDecimal.valueOf(i);
        if (value instanceof Long l) return BigDecimal.valueOf(l);
        throw new IllegalArgumentException("Неподдерживаемый тип для BigDecimal: " + value.getClass().getSimpleName());
    }
}