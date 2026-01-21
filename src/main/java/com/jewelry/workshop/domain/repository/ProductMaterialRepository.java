package com.jewelry.workshop.domain.repository;

import com.jewelry.workshop.domain.model.entity.ProductMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductMaterialRepository extends JpaRepository<ProductMaterial, Long> {
}