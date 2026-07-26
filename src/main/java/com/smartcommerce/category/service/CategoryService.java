package com.smartcommerce.category.service;

import com.smartcommerce.category.dto.request.CreateCategoryRequest;
import com.smartcommerce.category.dto.request.UpdateCategoryRequest;
import com.smartcommerce.category.dto.response.CategoryResponseDTO;
import com.smartcommerce.product.dto.response.ProductResponseDTO;

import java.util.List;

public interface CategoryService {

    CategoryResponseDTO createCategory(CreateCategoryRequest request);

    CategoryResponseDTO getCategoryById(Long id);

    CategoryResponseDTO updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);

    List<CategoryResponseDTO> getRootCategories();

    List<CategoryResponseDTO> getChildren(Long parentId);

    List<CategoryResponseDTO> getAllCategoriesForAdmin();

    CategoryResponseDTO setCategoryVisibility(Long id, boolean visible);
}
