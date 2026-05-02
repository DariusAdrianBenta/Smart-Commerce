package com.smartcommerce.category.controller;

import com.smartcommerce.category.dto.request.CreateCategoryRequest;
import com.smartcommerce.category.dto.request.UpdateCategoryRequest;
import com.smartcommerce.category.dto.response.CategoryResponseDTO;
import com.smartcommerce.category.entity.Category;
import com.smartcommerce.category.service.CategoryService;
import com.smartcommerce.product.dto.request.CreateProductRequest;
import com.smartcommerce.product.dto.request.ProductFilterDTO;
import com.smartcommerce.product.dto.response.ProductResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<CategoryResponseDTO>> getChildren(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getChildren(id));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategoriesForAdmin() {
        return ResponseEntity.ok(categoryService.getAllCategoriesForAdmin());
    }

    @PostMapping("/admin")
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

}
