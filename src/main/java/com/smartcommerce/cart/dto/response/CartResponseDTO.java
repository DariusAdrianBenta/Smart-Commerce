package com.smartcommerce.cart.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponseDTO {

    private Long id;

    private List<CartItemResponseDTO> items;

    private BigDecimal total;
}
