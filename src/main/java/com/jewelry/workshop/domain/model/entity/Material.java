package com.jewelry.workshop.domain.model.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "materials")
@Getter
@Setter
@ToString
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductMaterial> productMaterials = new HashSet<>();

    public void addProductMaterial(ProductMaterial pm){
        productMaterials.add(pm);
        pm.setMaterial(this);
    }

    public void removeProductMaterial(ProductMaterial pm){
        productMaterials.remove(pm);
        pm.setMaterial(null);
    }
}
