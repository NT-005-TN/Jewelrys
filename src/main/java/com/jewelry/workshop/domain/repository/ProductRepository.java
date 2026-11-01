package com.jewelry.workshop.domain.repository;

import com.jewelry.workshop.domain.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.inStock <= :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    @Query("""
        SELECT p,
               COUNT(oi) as orderCount,
               COALESCE(SUM(oi.quantity), 0) as totalSold,
               COALESCE(SUM(oi.totalPrice), 0) as totalRevenue
        FROM Product p 
        LEFT JOIN OrderItem oi ON p.id = oi.product.id 
        AND oi.createdAt BETWEEN :startDate AND :endDate
        AND oi.order.status IN ('COMPLETED', 'DELIVERED')
        GROUP BY p 
        ORDER BY totalRevenue DESC NULLS LAST
        """)
    List<Object[]> getProductSalesStatistics(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    @Query("""
    SELECT p FROM Product p WHERE 
    (:roleId <> 'CLIENT' OR p.isAvailable = true) AND 
    (:name IS NULL OR :name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND 
    (:type IS NULL OR :type = '' OR p.type = :type) AND 
    (:minPrice IS NULL OR p.price >= :minPrice) AND 
    (:maxPrice IS NULL OR p.price <= :maxPrice) AND 
    (:minWeight IS NULL OR p.weight >= :minWeight) AND 
    (:maxWeight IS NULL OR p.weight <= :maxWeight) AND 
    (:minStock IS NULL OR p.inStock >= :minStock) AND 
    (:isAvailable IS NULL OR p.isAvailable = :isAvailable)
    """)
    Page<Product> searchProducts(
            @Param("roleId") String roleId,
            @Param("name") String name,
            @Param("type") String type,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minWeight") BigDecimal minWeight,
            @Param("maxWeight") BigDecimal maxWeight,
            @Param("minStock") Integer minStock,
            @Param("isAvailable") Boolean isAvailable,
            Pageable pageable
    );


    @Query("SELECT p FROM Product p WHERE p.inStock <= :threshold")
    Page<Product> findByInStockLessThanEqual(@Param("threshold") Integer threshold, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE products
               SET in_stock = in_stock - :quantity,
                   is_available = (in_stock - :quantity) > 0,
                   updated_at = CURRENT_TIMESTAMP
             WHERE id = :productId
               AND is_available = TRUE
               AND in_stock >= :quantity
            """, nativeQuery = true)
    int decreaseStockIfAvailable(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
