package com.smartcommerce.product.service;

import com.smartcommerce.product.dto.request.CreateProductRequest;
import com.smartcommerce.product.dto.request.ProductFilterDTO;
import com.smartcommerce.product.dto.request.ProductUpdateImagesRequest;
import com.smartcommerce.product.dto.request.UpdateProductRequest;
import com.smartcommerce.product.dto.response.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {


    ProductResponseDTO createProduct(CreateProductRequest request);
    ProductResponseDTO getProductById(Long id);
    Page<ProductResponseDTO> getAllProducts(ProductFilterDTO filter, Pageable pageable);
    ProductResponseDTO updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id);
    void updateProductImages(Long id, ProductUpdateImagesRequest request);
}
