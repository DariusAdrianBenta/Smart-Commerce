package com.smartcommerce.product.service;

import com.smartcommerce.category.repository.CategoryRepository;
import com.smartcommerce.product.dto.response.ProductResponseDTO;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.entity.ProductStatus;
import com.smartcommerce.product.mapper.ProductMapper;
import com.smartcommerce.product.repository.ProductRepository;
import com.smartcommerce.product.service.impl.ProductServiceImpl;
import com.smartcommerce.productimage.mapper.ProductImageMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ProductServiceVisibilityTest {

    @Mock ProductRepository productRepository;
    @Mock ProductMapper productMapper;
    @Mock CategoryRepository categoryRepository;
    @Mock ProductImageMapper productImageMapper;
    @InjectMocks ProductServiceImpl productService;

    private Product product(long id, int stock, ProductStatus status, boolean hiddenByCat) {
        return Product.builder()
                .id(id).name("P").stock(stock).status(status)
                .hiddenByCategory(hiddenByCat).build();
    }

    @Test
    void setProductVisibility_false_hidesProduct() {
        Product p = product(1L, 10, ProductStatus.ACTIVE, false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(productMapper.toDTO(any(Product.class))).thenReturn(new ProductResponseDTO());

        productService.setProductVisibility(1L, false);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ProductStatus.DISABLED);
        assertThat(captor.getValue().isHiddenByCategory()).isFalse();
    }

    @Test
    void setProductVisibility_true_withStock_restoresActive() {
        Product p = product(1L, 5, ProductStatus.DISABLED, true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(productMapper.toDTO(any(Product.class))).thenReturn(new ProductResponseDTO());

        productService.setProductVisibility(1L, true);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(captor.getValue().isHiddenByCategory()).isFalse();
    }

    @Test
    void setProductVisibility_true_withoutStock_restoresOutOfStock() {
        Product p = product(1L, 0, ProductStatus.DISABLED, true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(productMapper.toDTO(any(Product.class))).thenReturn(new ProductResponseDTO());

        productService.setProductVisibility(1L, true);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
    }
}
