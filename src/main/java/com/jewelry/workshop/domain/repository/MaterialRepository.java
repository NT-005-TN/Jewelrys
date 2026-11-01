package com.jewelry.workshop.domain.repository;

import com.jewelry.workshop.domain.model.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    boolean existsByName(String name);

    @Query("""
        SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END
        FROM ProductMaterial pm
        WHERE pm.material.id = :materialId
        """)
    boolean isMaterialInUse(@Param("materialId") Long materialId);
}