package com.jewelry.workshop.service.impl;

import com.jewelry.workshop.domain.model.entity.Client;
import com.jewelry.workshop.service.interfaces.DiscountService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DiscountServiceImpl implements DiscountService {

    @Override
    public BigDecimal calculateDiscount(BigDecimal totalAmount, Client client) {
        if (client != null && Boolean.TRUE.equals(client.getIsPermanent())) {
            return totalAmount.multiply(new BigDecimal("0.10")); // 10%
        }
        return BigDecimal.ZERO;
    }
}
