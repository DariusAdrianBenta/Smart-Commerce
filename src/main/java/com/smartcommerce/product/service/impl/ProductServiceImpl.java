package com.smartcommerce.product.service.impl;

import com.smartcommerce.category.entity.Category;
import com.smartcommerce.category.repository.CategoryRepository;
import com.smartcommerce.exception.CategoryNotFoundException;
import com.smartcommerce.exception.ProductNotFoundException;
import com.smartcommerce.product.dto.request.CreateProductRequest;
import com.smartcommerce.product.dto.request.ProductFilterDTO;
import com.smartcommerce.product.dto.request.ProductUpdateImagesRequest;
import com.smartcommerce.product.dto.request.UpdateProductRequest;
import com.smartcommerce.product.dto.response.ProductResponseDTO;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.entity.ProductStatus;
import com.smartcommerce.product.mapper.ProductMapper;
import com.smartcommerce.product.repository.ProductRepository;
import com.smartcommerce.product.repository.ProductSpecification;
import com.smartcommerce.product.service.ProductService;
import com.smartcommerce.productimage.entity.ProductImage;
import com.smartcommerce.productimage.mapper.ProductImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final ProductImageMapper productImageMapper;

    @Override
    public ProductResponseDTO createProduct(CreateProductRequest request) {

        Product product = productMapper.toEntity(request);

        Category category = getCategoryOrThrow(request.getCategoryId());

        if (!category.isActive()) {
            throw new IllegalStateException("La categoría está deshabilitada");
        }

        product.setCategory(category);
        product.setStock(request.getStock());

        updateProductStatusBasedOnStock(product);

        Product savedProduct = productRepository.save(product);

        return productMapper.toDTO(savedProduct);
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        Product product = getProductOrThrow(id);
        return productMapper.toDTO(product);
    }

    @Override
    public Page<ProductResponseDTO> getAllProducts(ProductFilterDTO filter, Pageable pageable) {

        // Partimos excluyendo los productos deshabilitados (borrado lógico).
        Specification<Product> spec = ProductSpecification.isNotDisabled();

        if (filter.getMinPrice() != null) {
            spec = spec.and(ProductSpecification.hasMinPrice(filter.getMinPrice()));
        }

        if (filter.getMaxPrice() != null) {
            spec = spec.and(ProductSpecification.hasMaxPrice(filter.getMaxPrice()));
        }

        if (filter.getBrand() != null && !filter.getBrand().isBlank()) {
            spec = spec.and(ProductSpecification.hasBrand(filter.getBrand()));
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            spec = spec.and(ProductSpecification.nameContains(filter.getName()));
        }

        if (filter.getCategoryId() != null) {
            spec = spec.and(ProductSpecification.hasCategory(filter.getCategoryId()));
        }

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        return productPage.map(productMapper::toDTO);
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, UpdateProductRequest request) {

        Product product = getProductOrThrow(id);

        if (request.getName() != null) {
            product.setName(request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }

        if (request.getBrand() != null) {
            product.setBrand(request.getBrand());
        }

        if (request.getCategoryId() != null) {
            Category category = getCategoryOrThrow(request.getCategoryId());

            if (!category.isActive()) {
                throw new IllegalStateException("No puedes asignar una categoría inactiva");
            }

            product.setCategory(category);
        }

        updateProductStatusBasedOnStock(product);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {

        Product product = getProductOrThrow(id);

        if (product.getStatus() != ProductStatus.DISABLED) {
            product.setStatus(ProductStatus.DISABLED);
            productRepository.save(product);
        }
    }

    @Override
    @Transactional
    public void updateProductImages(Long id, ProductUpdateImagesRequest request) {

        Product product = getProductOrThrow(id);

        product.getImages().clear();

        List<ProductImage> newImages = request.getImages().stream()
                .map(imageRequest -> {
                    ProductImage image = productImageMapper.toEntity(imageRequest);
                    image.setProduct(product);
                    return image;
                })
                .toList();

        product.getImages().addAll(newImages);

        productRepository.save(product);
    }

    @Override
    public List<ProductResponseDTO> getAllProductsForAdmin() {
        return productRepository.findAll().stream()
                .map(productMapper::toDTO)
                .toList();
    }

    @Override
    public ProductResponseDTO setProductVisibility(Long id, boolean visible) {
        Product product = getProductOrThrow(id);
        product.setHiddenByCategory(false);
        if (visible) {
            product.setStatus(product.getStock() != null && product.getStock() > 0
                    ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK);
        } else {
            product.setStatus(ProductStatus.DISABLED);
        }
        Product saved = productRepository.save(product);
        return productMapper.toDTO(saved);
    }

    private void updateProductStatusBasedOnStock(Product product) {
        if (product.getStatus() == ProductStatus.DISABLED) {
            return;
        }

        if (product.getStock() == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else {
            product.setStatus(ProductStatus.ACTIVE);
        }
    }

    private Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
