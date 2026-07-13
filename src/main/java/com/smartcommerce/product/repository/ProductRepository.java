package com.smartcommerce.product.repository;

import com.smartcommerce.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Long> , JpaSpecificationExecutor<Product> {

    // Usado por el seeder de catálogo para no crear productos duplicados.
    boolean existsByName(String name);
}
