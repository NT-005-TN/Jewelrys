package com.jewelry.workshop.service.impl;

import com.jewelry.workshop.domain.model.dto.product.*;
import com.jewelry.workshop.domain.model.entity.Product;
import com.jewelry.workshop.domain.repository.ProductRepository;
import com.jewelry.workshop.service.interfaces.ProductService;
import com.jewelry.workshop.service.mapper.ProductMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private  final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public ProductSearchResponseDTO searchProducts(String userRole, ProductSearchDTO criteria, int page, int size) {
        String sortByField = switch (criteria.getSortBy()) {
            case "price" -> "price";
            case "weight" -> "weight";
            case "stock" -> "inStock";
            case "created" -> "createdAt";
            default -> "name";
        };

        Sort.Direction direction = "desc".equalsIgnoreCase(criteria.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortByField));

        Page<Product> products = productRepository.searchProducts(
                userRole,
                criteria.getName(),
                criteria.getType(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getMinWeight(),
                criteria.getMaxWeight(),
                criteria.getMinStock(),
                criteria.getIsAvailable(),
                pageable
        );

        List<ProductResponseDTO> items = products.getContent().stream()
                .map(productMapper::toDto)
                .toList();

        return new ProductSearchResponseDTO() {{
            setItems(items);
            setPage(products.getNumber());
            setSize(products.getSize());
            setTotalElements(products.getTotalElements());
            setTotalPages(products.getTotalPages());
            setFirst(products.isFirst());
            setLast(products.isLast());
            setEmpty(products.isEmpty());
        }};
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "products",
            key = "{#p0, #userRole}",
            unless = "#result == null || #result.price.compareTo(T(java.math.BigDecimal).valueOf(200000L)) > 0"
    )
    public ProductResponseDTO getProductById(Long productId, String userRole) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("ID изделия должен быть положительным числом");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Изделие не найдено"));

        if ("CLIENT".equals(userRole) && !Boolean.TRUE.equals(product.getIsAvailable())) {
            throw new EntityNotFoundException("Изделие не найдено или недоступно");
        }

        return productMapper.toDto(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductCreateDTO dto) {
        if (dto.getSku() != null && productRepository.existsBySku(dto.getSku())) {
            throw new RuntimeException("Артикул " + dto.getSku() + " уже существует");
        }
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setSku(dto.getSku());
        product.setWeight(dto.getWeight());
        product.setPrice(dto.getPrice());
        product.setType(dto.getType());
        product.setInStock(dto.getInStock());
        product.setIsAvailable(dto.getInStock() > 0);
        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Изделие не найдено"));

        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getWeight() != null) product.setWeight(dto.getWeight());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getType() != null) product.setType(dto.getType());
        if (dto.getInStock() != null) {
            product.setInStock(dto.getInStock());
            product.setIsAvailable(dto.getInStock() > 0);
        }
        if (dto.getIsAvailable() != null) product.setIsAvailable(dto.getIsAvailable());

        Product updated = productRepository.save(product);
        return productMapper.toDto(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Изделие не найдено");
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateInStock(Long id, Integer newStock){
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID изделия должен быть положительным числом");
        }
        if (newStock == null) {
            throw new IllegalArgumentException("Остаток не может быть null");
        }
        if (newStock < 0) {
            throw new IllegalArgumentException("Остаток не может быть отрицательным");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Изделие не найдено"));

        product.setInStock(newStock);
        product.setIsAvailable(newStock > 0);
        productRepository.save(product);

    }

    @Override
    @Transactional(readOnly = true)
    public ProductSearchResponseDTO getLowStockProducts(int page, int size, Integer threshold) {
        int actualThreshold = (threshold != null && threshold >= 0) ? threshold : 5;
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByInStockLessThanEqual(actualThreshold, pageable);

        List<ProductResponseDTO> items = products.getContent().stream()
                .map(productMapper::toDto)
                .toList();

        return new ProductSearchResponseDTO() {{
            setItems(items);
            setPage(products.getNumber());
            setSize(products.getSize());
            setTotalElements(products.getTotalElements());
            setTotalPages(products.getTotalPages());
            setFirst(products.isFirst());
            setLast(products.isLast());
            setEmpty(products.isEmpty());
        }};
    }

}
