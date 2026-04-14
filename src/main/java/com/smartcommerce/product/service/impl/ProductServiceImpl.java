package com.smartcommerce.product.service.impl;

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
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
  //  private final CategoryRepository categoryRepository;


    @Override
    public ProductResponseDTO createProduct(CreateProductRequest request) {


        Product product = productMapper.toEntity(request);


        product.setStock(request.getStock());


        updateProductStatusBasedOnStock(product);

        Product savedProduct = productRepository.save(product);

        return productMapper.toDTO(savedProduct);

    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.toDTO(product);

    }


    @Override
    public Page<ProductResponseDTO> getAllProducts(
            ProductFilterDTO filter,
            Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();
        if(filter.getMinPrice() != null){
            spec = spec.and(ProductSpecification.hasMinPrice(filter.getMinPrice()));
        }
        if (filter.getMaxPrice() != null ) {
            spec = spec.and(ProductSpecification.hasMaxPrice(filter.getMaxPrice()));
        }

        if (filter.getBrand() != null && !filter.getBrand().isBlank()) {
            spec = spec.and(ProductSpecification.hasBrand(filter.getBrand()));
        }

        if (filter.getName() != null && !filter.getName().isBlank()) {
            spec = spec.and(ProductSpecification.nameContains(filter.getName()));
        }


        Page<Product> productPage = productRepository.findAll(spec,pageable);
        return productPage.map(productMapper::toDTO);
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, UpdateProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setBrand(request.getBrand());

        // lógica de negocio → SIEMPRE después de cambiar stock
        updateProductStatusBasedOnStock(product);

        Product updatedProduct = productRepository.save(product);

        return productMapper.toDTO(updatedProduct);

    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setStatus(ProductStatus.DISABLED);
        productRepository.save(product);
    }
//TODO DESCOMENTAR CUANDO TENGA LO DE PRODUCT_IMAGES

   @Override
    public void updateProductImages(Long id, ProductUpdateImagesRequest request) {

//        Product product = productRepository.findById(id)
//                .orElseThrow(() -> new ProductNotFoundException(id));
//
//         1. Limpiar imágenes actuales
//        product.getImages().clear();
//
//         2. Crear nuevas imágenes
//        List<ProductImage> newImages = request.getImageUrls().stream()
//                .map(url -> {
//                    ProductImage image = new ProductImage();
//                    image.setImageUrl(url);
//                    image.setProduct(product);  relación importante
//                    return image;
//                })
//                .toList();
//
//         3. Añadir nuevas imágenes
//        product.getImages().addAll(newImages);
//
//         4. Guardar (cascade se encarga)
//        productRepository.save(product);
}

    private void updateProductStatusBasedOnStock(Product product) {
        //No modificamos el status si esta eliminado
        if (product.getStatus() == ProductStatus.DISABLED) {
            return;
        }

        if (product.getStock() == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else {
            product.setStatus(ProductStatus.ACTIVE);
        }
    }
}
