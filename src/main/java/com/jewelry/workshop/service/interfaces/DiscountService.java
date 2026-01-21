package com.jewelry.workshop.service.interfaces;

import com.jewelry.workshop.domain.model.entity.Client;
import java.math.BigDecimal;

public interface DiscountService {
    BigDecimal calculateDiscount(BigDecimal totalAmount, Client client);
}