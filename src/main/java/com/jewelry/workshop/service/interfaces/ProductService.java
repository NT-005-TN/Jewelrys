package com.jewelry.workshop.service.interfaces;

import com.jewelry.workshop.domain.model.dto.product.*;

public interface ProductService {
    ProductSearchResponseDTO searchProducts(String userRole, ProductSearchDTO criteria, int page, int size);
    ProductResponseDTO getProductById(Long productId, String userRole);

    ProductResponseDTO createProduct(ProductCreateDTO dto);
    ProductResponseDTO updateProduct(Long id, ProductUpdateDTO dto);

    void deleteProduct(Long id);
    void updateInStock(Long id, Integer newStock);

    ProductSearchResponseDTO getLowStockProducts(int page, int size, Integer threshold);
}
