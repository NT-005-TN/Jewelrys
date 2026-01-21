package com.jewelry.workshop.domain.repository;

import com.jewelry.workshop.domain.model.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByUserId(Long userId);
    Optional<Client> findByPhone(String phone);

    void deleteByUserId(Long userId);

    @Query("SELECT COUNT(c) FROM Client c WHERE c.isPermanent = true")
    Long countPermanentClients();

    @Query("SELECT COUNT(c) FROM Client c WHERE c.createdAt >= :date")
    Long countNewClientsSince(@Param("date") Instant date);

    @Query("""
    SELECT c FROM Client c
    WHERE
    (:lastName IS NULL OR FUNCTION('LOWER', CAST(c.lastName AS text)) LIKE LOWER(CONCAT('%', CAST(:lastName AS text), '%')))
    AND (:firstName IS NULL OR FUNCTION('LOWER', CAST(c.firstName AS text)) LIKE LOWER(CONCAT('%', CAST(:firstName AS text), '%')))
    AND (:phonePattern IS NULL OR c.phone LIKE :phonePattern)
    AND (:isPermanent IS NULL OR c.isPermanent = :isPermanent)
    AND (:minOrders IS NULL OR c.id IN (
        SELECT o.client.id FROM Order o
        GROUP BY o.client.id
        HAVING COUNT(o) >= :minOrders
    ))
    ORDER BY
    CASE WHEN :sortBy = 'name' THEN c.lastName END,
    CASE WHEN :sortBy = 'created' THEN c.createdAt END DESC,
    CASE WHEN :sortBy = 'orders' THEN (
        SELECT COUNT(o2) FROM Order o2 WHERE o2.client.id = c.id
    ) END DESC
    """)
    Page<Client> findClientsByCriteria(
            @Param("lastName") String lastName,
            @Param("firstName") String firstName,
            @Param("phonePattern") String phonePattern,
            @Param("isPermanent") Boolean isPermanent,
            @Param("minOrders") Integer minOrders,
            @Param("sortBy") String sortBy,
            Pageable pageable
    );

    @Query("""
    SELECT c,
       COUNT(o) as totalOrders,
       SUM(o.finalAmount) as totalSpent,
       MAX(o.orderDatetime) as lastOrderDate
    FROM Client c
    LEFT JOIN c.orders o
    WHERE (o.orderDatetime BETWEEN :startDate AND :endDate) OR o IS NULL
    GROUP BY c.id, c.firstName, c.lastName, c.patronymic, c.phone, c.isPermanent, c.createdAt, c.updatedAt, c.user
    HAVING (COUNT(o) >= :minOrders OR :minOrders IS NULL)
    ORDER BY SUM(o.finalAmount) DESC NULLS LAST
    """)
    List<Object[]> getClientStatistics(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("minOrders") Integer minOrders
    );

    @Query("""
        SELECT c, 
               (SELECT COUNT(o) FROM Order o WHERE o.client.id = c.id AND o.status = 'COMPLETED') as completedOrders,
               (SELECT SUM(o.finalAmount) FROM Order o WHERE o.client.id = c.id) as lifetimeValue,
               (SELECT MAX(o.orderDatetime) FROM Order o WHERE o.client.id = c.id) as lastPurchaseDate
        FROM Client c 
        WHERE c.isPermanent = true
        ORDER BY lifetimeValue DESC
        """)
    List<Object[]> getPermanentClientDetails(Pageable pageable);
}