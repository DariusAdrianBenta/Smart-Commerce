package com.smartcommerce.cart.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.smartcommerce.cart.dto.response.CartItemResponseDTO;
import com.smartcommerce.cart.dto.response.CartResponseDTO;
import com.smartcommerce.cart.entity.Cart;
import com.smartcommerce.cart.entity.CartItem;
import com.smartcommerce.productimage.entity.ProductImage;

@Mapper(componentModel = "spring")
public interface CartMapper {

    // El total no existe en la entidad: se calcula en @AfterMapping a partir de los subtotales.
    @Mapping(target = "total", ignore = true)
    CartResponseDTO toDTO(Cart cart);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productPrice", source = "product.price")
    @Mapping(target = "productImage", source = "product.images", qualifiedByName = "firstImage")
    @Mapping(target = "subtotal", expression = "java(item.getProduct().getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    CartItemResponseDTO toItemDTO(CartItem item);

    @Named("firstImage")
    default String firstImage(List<ProductImage> images) {
        return (images == null || images.isEmpty()) ? null : images.get(0).getImageUrl();
    }

    @AfterMapping
    default void calcTotal(Cart cart, @MappingTarget CartResponseDTO dto) {
        BigDecimal total = dto.getItems().stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotal(total);
    }
}
