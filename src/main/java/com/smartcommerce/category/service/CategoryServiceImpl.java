package com.smartcommerce.category.service;

import com.smartcommerce.category.dto.request.CreateCategoryRequest;
import com.smartcommerce.category.dto.request.UpdateCategoryRequest;
import com.smartcommerce.category.dto.response.CategoryResponseDTO;
import com.smartcommerce.category.entity.Category;
import com.smartcommerce.category.mapper.CategoryMapper;
import com.smartcommerce.category.repository.CategoryRepository;
import com.smartcommerce.common.SlugUtils;
import com.smartcommerce.exception.CategoryNotFoundException;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.entity.ProductStatus;
import com.smartcommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;


    @Override
    public CategoryResponseDTO createCategory(CreateCategoryRequest request) {

        Category category = categoryMapper.toEntity(request);
        // El request no trae slug ni active: se generan aquí.
        category.setSlug(SlugUtils.toSlug(request.getName()));
        category.setActive(true);

        if (request.getParentId() != null) {

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getParentId()));

            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toDTO(savedCategory);
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        return categoryMapper.toDTO(category);
    }

    @Override
    public CategoryResponseDTO updateCategory(Long id, UpdateCategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));;


        if (request.getName() != null) {
            category.setName(request.getName());
        }

        if (request.getParentId() != null) {

            if (request.getParentId().equals(id)) {
                throw new IllegalStateException("Una categoría no puede ser su propio padre");
            }

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getParentId()));

            validateNoCycle(category, parent);

            category.setParent(parent);

        } else {

            category.setParent(null);
        }

        Category updatedCategory = categoryRepository.save(category);

        return categoryMapper.toDTO(updatedCategory);
    }
    private void validateNoCycle(Category category, Category newParent) {

        Category current = newParent;

        while (current != null) {
            if (current.getId().equals(category.getId())) {
                throw new IllegalStateException("No se puede crear un ciclo en la jerarquía de categorías");
            }
            current = current.getParent();
        }
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        if (categoryRepository.existsByParentAndActiveTrue(category)) {
            throw new IllegalStateException("No puedes eliminar una categoría con subcategorías activas");
        }

        category.setActive(false);

        categoryRepository.save(category);

    }

    @Override
    public List<CategoryResponseDTO> getRootCategories() {
        List<Category> categories = categoryRepository.findByParentIsNullAndActiveTrue();

        return categories.stream()
                .map(categoryMapper::toDTO)
                .toList();
    }

    @Override
    public List<CategoryResponseDTO> getChildren(Long parentId) {

        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new CategoryNotFoundException(parentId));


        List<Category> children = categoryRepository.findByParentAndActiveTrue(parent);


        return children.stream()
                .map(categoryMapper::toDTO)
                .toList();
    }

    @Override
    public List<CategoryResponseDTO> getAllCategoriesForAdmin() {

        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(categoryMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponseDTO setCategoryVisibility(Long id, boolean visible) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (visible) {
            category.setActive(true);
            for (Product product : productRepository.findByCategory(category)) {
                if (product.isHiddenByCategory()) {
                    product.setHiddenByCategory(false);
                    product.setStatus(product.getStock() != null && product.getStock() > 0
                            ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK);
                    productRepository.save(product);
                }
            }
        } else {
            if (categoryRepository.existsByParentAndActiveTrue(category)) {
                throw new IllegalStateException(
                        "No puedes ocultar una categoría con subcategorías activas");
            }
            category.setActive(false);
            for (Product product : productRepository.findByCategory(category)) {
                if (product.getStatus() != ProductStatus.DISABLED) {
                    product.setStatus(ProductStatus.DISABLED);
                    product.setHiddenByCategory(true);
                    productRepository.save(product);
                }
            }
        }

        Category saved = categoryRepository.save(category);
        return categoryMapper.toDTO(saved);
    }
}
