package com.smartcommerce.category.controller;

import com.smartcommerce.category.dto.request.CreateCategoryRequest;
import com.smartcommerce.category.dto.request.UpdateCategoryRequest;
import com.smartcommerce.category.dto.response.CategoryResponseDTO;
import com.smartcommerce.category.service.CategoryService;
import com.smartcommerce.common.dto.VisibilityRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategoriesForAdmin() {
        return ResponseEntity.ok(categoryService.getAllCategoriesForAdmin());
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/{id}/visibility")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> setCategoryVisibility(
            @PathVariable Long id,
            @Valid @RequestBody VisibilityRequest request) {
        return ResponseEntity.ok(
                categoryService.setCategoryVisibility(id, request.getVisible()));
    }

}
