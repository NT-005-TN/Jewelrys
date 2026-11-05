package com.jewelry.workshop.service.impl;

import com.jewelry.workshop.domain.model.dto.order.OrderCreateDTO;
import com.jewelry.workshop.domain.model.dto.orderitem.OrderItemCreateDTO;
import com.jewelry.workshop.domain.model.entity.*;
import com.jewelry.workshop.domain.repository.*;
import com.jewelry.workshop.service.interfaces.DiscountService;
import com.jewelry.workshop.service.mapper.OrderMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    @Test void lastItemCanBePurchasedAtItsRecordedPrice() {
        OrderRepository orders = mock(OrderRepository.class);
        ClientRepository clients = mock(ClientRepository.class);
        ProductRepository products = mock(ProductRepository.class);
        DiscountService discounts = mock(DiscountService.class);
        OrderServiceImpl service = new OrderServiceImpl(orders, clients, products,
                mock(OrderItemRepository.class), mock(OrderMapper.class), mock(UserRepository.class), discounts);
        Client client = new Client();
        when(clients.findById(1L)).thenReturn(Optional.of(client));
        Product product = new Product();
        product.setIsAvailable(true);
        product.setInStock(1);
        product.setPrice(new BigDecimal("1200.00"));
        when(products.findLockedById(7L)).thenReturn(Optional.of(product));
        when(discounts.calculateDiscount(any(), eq(client))).thenReturn(BigDecimal.ZERO);
        when(orders.saveAndFlush(any())).thenAnswer(call -> {
            Order order = call.getArgument(0);
            assertEquals(new BigDecimal("1200.00"), order.getFinalAmount());
            assertEquals(1, order.getOrderItems().size());
            return order;
        });
        OrderItemCreateDTO item = new OrderItemCreateDTO();
        item.setProductId(7L);
        item.setQuantity(1);
        OrderCreateDTO request = new OrderCreateDTO();
        request.setItems(List.of(item));
        assertDoesNotThrow(() -> service.createOrder(1L, request));
        verify(orders).saveAndFlush(any());
        assertEquals(0, product.getInStock());
    }

    @Test void nullOrderItemsFailValidation() {
        OrderCreateDTO request = new OrderCreateDTO();
        request.setItems(Arrays.asList((OrderItemCreateDTO) null));
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertFalse(factory.getValidator().validate(request).isEmpty());
        }
    }
}
