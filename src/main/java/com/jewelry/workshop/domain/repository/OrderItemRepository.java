package com.jewelry.workshop.domain.repository;

import com.jewelry.workshop.domain.model.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.time.Instant;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            SELECT oi.product.id, SUM(oi.quantity) as totalQuantity, SUM(oi.totalPrice) as totalRevenue
            FROM OrderItem oi
            WHERE oi.createdAt BETWEEN :startDate and :endDate
            GROUP BY oi.product.id
            ORDER BY totalQuantity DESC
            """)
    Page<Object[]> findBestSellingProductsInPeriod(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );
}
