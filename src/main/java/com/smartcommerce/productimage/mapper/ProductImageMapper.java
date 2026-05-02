package com.smartcommerce.productimage.mapper;

import com.smartcommerce.product.dto.request.ProductImageRequest;
import com.smartcommerce.productimage.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductImage toEntity(ProductImageRequest dto);
}
