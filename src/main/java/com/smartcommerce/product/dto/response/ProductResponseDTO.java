package com.smartcommerce.product.dto.response;

import com.smartcommerce.product.entity.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class ProductResponseDTO {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    private ProductStatus status;

    private String brand;

    private LocalDateTime createdAt;

    private Long categoryId;

    private String categoryName;

    private List<String> imageUrls;
}
