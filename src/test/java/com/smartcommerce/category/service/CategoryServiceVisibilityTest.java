package com.smartcommerce.category.service;

import com.smartcommerce.category.dto.response.CategoryResponseDTO;
import com.smartcommerce.category.entity.Category;
import com.smartcommerce.category.mapper.CategoryMapper;
import com.smartcommerce.category.repository.CategoryRepository;
import com.smartcommerce.category.service.CategoryServiceImpl;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.entity.ProductStatus;
import com.smartcommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceVisibilityTest {

    @Mock CategoryRepository categoryRepository;
    @Mock CategoryMapper categoryMapper;
    @Mock ProductRepository productRepository;
    @InjectMocks CategoryServiceImpl categoryService;

    private Category category(long id, boolean active) {
        Category c = new Category();
        c.setId(id);
        c.setName("Cat");
        c.setActive(active);
        return c;
    }

    private Product product(long id, ProductStatus status, boolean hiddenByCat) {
        return Product.builder().id(id).name("P").stock(5)
                .status(status).hiddenByCategory(hiddenByCat).build();
    }

    @Test
    void hide_withoutActiveSubcategories_disablesCategoryAndCascadesProducts() {
        Category c = category(1L, true);
        Product visible = product(10L, ProductStatus.ACTIVE, false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));
        when(categoryRepository.existsByParentAndActiveTrue(c)).thenReturn(false);
        when(productRepository.findByCategory(c)).thenReturn(List.of(visible));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));
        when(categoryMapper.toDTO(any(Category.class))).thenReturn(new CategoryResponseDTO());

        categoryService.setCategoryVisibility(1L, false);

        assertThat(c.isActive()).isFalse();
        assertThat(visible.getStatus()).isEqualTo(ProductStatus.DISABLED);
        assertThat(visible.isHiddenByCategory()).isTrue();
        verify(productRepository).save(visible);
    }

    @Test
    void hide_withActiveSubcategories_throws() {
        Category c = category(1L, true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));
        when(categoryRepository.existsByParentAndActiveTrue(c)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.setCategoryVisibility(1L, false))
                .isInstanceOf(IllegalStateException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void show_restoresOnlyCascadeHiddenProducts() {
        Category c = category(1L, false);
        Product byCascade = product(10L, ProductStatus.DISABLED, true);
        Product byHand = product(11L, ProductStatus.DISABLED, false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));
        when(productRepository.findByCategory(c)).thenReturn(List.of(byCascade, byHand));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));
        when(categoryMapper.toDTO(any(Category.class))).thenReturn(new CategoryResponseDTO());

        categoryService.setCategoryVisibility(1L, true);

        assertThat(c.isActive()).isTrue();
        assertThat(byCascade.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(byCascade.isHiddenByCategory()).isFalse();
        assertThat(byHand.getStatus()).isEqualTo(ProductStatus.DISABLED); // sigue oculto a mano
        verify(productRepository).save(byCascade);
        verify(productRepository, never()).save(byHand);
    }
}
