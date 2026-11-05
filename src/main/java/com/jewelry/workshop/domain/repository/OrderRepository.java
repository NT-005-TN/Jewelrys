package com.jewelry.workshop.domain.repository;

import com.jewelry.workshop.domain.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Order e where e.id = :id")
    java.util.Optional<Order> findLockedById(@Param("id") Long id);

    List<Order> findByStatus(String status);
    @Query("select coalesce(sum(o.discountAmount), 0) from Order o where o.status in ('COMPLETED','DELIVERED') and o.orderDatetime between :start and :end")
    BigDecimal sumDiscounts(@Param("start") Instant start, @Param("end") Instant end);
    @Query("select coalesce(sum(o.finalAmount), 0) from Order o where o.status in ('COMPLETED','DELIVERED')")
    BigDecimal lifetimeRevenue();
    List<Order> findByOrderDatetimeBetween(Instant start, Instant end);

    Page<Order> findByClientIdAndStatusNot(Long clientId, String status, Pageable pageable);
    Page<Order> findByClientId(Long ClientId, Pageable pageable);

    @Query("""
        SELECT DATE(CAST(o.orderDatetime AS date)) as orderDate, 
               COUNT(o) as orderCount,
               SUM(o.finalAmount) as dailyRevenue,
               AVG(o.finalAmount) as avgOrderValue
        FROM Order o 
        WHERE o.status IN ('COMPLETED', 'DELIVERED') AND o.orderDatetime >= :startDate
        GROUP BY DATE(CAST(o.orderDatetime AS date))
        ORDER BY orderDate DESC
        """)
    List<Object[]> getDailyRevenue(@Param("startDate") Instant startDate);

    @Query("""
    SELECT o.client.id, c.lastName, c.firstName, 
           COUNT(o) as orderCount,
           SUM(o.finalAmount) as totalSpent,
           MAX(o.orderDatetime) as lastOrderDate
    FROM Order o 
    JOIN o.client c 
    WHERE o.status IN ('COMPLETED', 'DELIVERED') AND o.orderDatetime BETWEEN :startDate AND :endDate
    GROUP BY o.client.id, c.lastName, c.firstName 
    ORDER BY totalSpent DESC
    """)
    List<Object[]> getClientStatisticsInPeriod(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("SELECT COUNT(o) FROM Order o WHERE o.client.id = :clientId")
    Long countOrdersByClientId(@Param("clientId") Long clientId);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status IN ('COMPLETED', 'DELIVERED') AND o.client.id = :clientId")
    BigDecimal getTotalSpentByClientId(@Param("clientId") Long clientId);

    @Query("SELECT MAX(o.orderDatetime) FROM Order o WHERE o.client.id = :clientId")
    Instant getLastOrderDateByClientId(@Param("clientId") Long clientId);

    @Query("""
        SELECT o FROM Order o 
        WHERE (:clientId IS NULL OR o.client.id = :clientId)
        AND (:status IS NULL OR o.status = :status)
        AND (:startDate IS NULL OR o.orderDatetime >= :startDate)
        AND (:endDate IS NULL OR o.orderDatetime <= :endDate)
        AND (:minAmount IS NULL OR o.finalAmount >= :minAmount)
        AND (:maxAmount IS NULL OR o.finalAmount <= :maxAmount)
        ORDER BY o.orderDatetime DESC
        """)
    Page<Order> findOrdersByCriteria(
            @Param("clientId") Long clientId,
            @Param("status") String status,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable
    );
}