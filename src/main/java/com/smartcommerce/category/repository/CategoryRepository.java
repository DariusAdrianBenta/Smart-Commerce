package com.smartcommerce.category.repository;

import com.smartcommerce.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

  // Active / Inactive
    List<Category> findByActiveTrue();
    List<Category> findByActiveFalse();

    // (USER)
    List<Category> findByParentAndActiveTrue(Category parent);
    List<Category> findByParentIsNullAndActiveTrue();

    // (ADMIN)
    List<Category> findByParent(Category parent);
    List<Category> findByParentAndActiveFalse(Category parent);

    // Para saber si la categoría tiene hijos activos
    boolean existsByParentAndActiveTrue(Category parent);
}
