package com.smartcommerce.category.service;

import com.smartcommerce.category.dto.request.CreateCategoryRequest;
import com.smartcommerce.category.dto.request.UpdateCategoryRequest;
import com.smartcommerce.category.dto.response.CategoryResponseDTO;
import com.smartcommerce.category.entity.Category;
import com.smartcommerce.category.mapper.CategoryMapper;
import com.smartcommerce.category.repository.CategoryRepository;
import com.smartcommerce.exception.CategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    @Override
    public CategoryResponseDTO createCategory(CreateCategoryRequest request) {

        Category category = categoryMapper.toEntity(request);

        if (request.getParentId() != null) {

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));

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
        return null;
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        category.setActive(false);

        categoryRepository.save(category);

    }

    @Override
    public List<CategoryResponseDTO> getRootCategories() {
        return List.of();
    }

    @Override
    public List<CategoryResponseDTO> getChildren(Long parentId) {
        return List.of();
    }
}
