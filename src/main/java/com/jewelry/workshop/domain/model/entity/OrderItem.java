package com.jewelry.workshop.domain.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@ToString
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public OrderItem() {
        this.quantity = 1;
        this.unitPrice = BigDecimal.ZERO;
        this.totalPrice = BigDecimal.ZERO;
    }

    public OrderItem(Product product, Integer quantity){
        this();
        setProduct(product);
        setQuantity(quantity);
    }

    public void calculateTotalPrice(){
        if (unitPrice != null && quantity != null)
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        else
            totalPrice = BigDecimal.ZERO;
    }

    public void setProduct(Product product){
        this.product = product;
        if (product != null) {
            this.unitPrice = product.getPrice();
            calculateTotalPrice();
        }
    }

    public void setQuantity(Integer quantity){
        if (quantity == null || quantity <= 0){
            throw new IllegalArgumentException(
                    "Количество должно быть положительным"
            );
        }
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public void updateUnitPrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException(
                    "Цена не может быть отрицательной"
            );
        }
        this.unitPrice = newPrice;
        calculateTotalPrice();
    }

    public void applyDiscount(BigDecimal discountPercent){
        if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0){
            BigDecimal discount = unitPrice.multiply(
                    discountPercent.divide(new BigDecimal("100")));
                    unitPrice = unitPrice.subtract(discount).max(BigDecimal.ZERO);
            calculateTotalPrice();
        }
    }

    public String getItemInfo(){
        return String.format("%s × %d = %,.2f руб.",
                product != null ? product.getName() : "Неизвестный товар",
                quantity,
                totalPrice != null ? totalPrice.doubleValue() : 0.0);
    }

}
