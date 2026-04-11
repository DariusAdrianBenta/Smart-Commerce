package com.smartcommerce.product.dto.request;

import lombok.*;

import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFilterDTO {

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String brand;
    private String name;
}
