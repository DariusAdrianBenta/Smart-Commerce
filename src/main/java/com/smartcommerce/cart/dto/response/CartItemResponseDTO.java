package com.smartcommerce.cart.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponseDTO {

    private Long productId;

    private String productName;

    private BigDecimal productPrice;

    private String productImage;

    private Integer quantity;

    private BigDecimal subtotal;
}
