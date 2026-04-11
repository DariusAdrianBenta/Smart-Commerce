package com.smartcommerce.product.repository;

import com.smartcommerce.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> hasMinPrice(BigDecimal minPrice){
        return (root,query,cb) ->
                cb.greaterThanOrEqualTo(root.get("price"),minPrice);
    }
    public static Specification<Product> hasMaxPrice(BigDecimal maxPrice){
        return (root,query,cb) ->
                cb.greaterThanOrEqualTo(root.get("price"),maxPrice);
    }

    public static Specification<Product> hasBrand(String brand){
        return (root, query, cb) ->
                cb.equal(root.get("brand"),brand);
    }

    public static Specification<Product> nameContains(String name){
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }
    public static Specification<Product> hasStockGreaterThan(Integer stock) {
        return (root, query, cb) ->
                cb.greaterThan(root.get("stock"), stock);
    }
}
