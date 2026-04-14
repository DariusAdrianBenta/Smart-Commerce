package com.smartcommerce.product.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProductUpdateImagesRequest {
    // Campo obligatorio, debe haber al menos una imagen
    @NotEmpty(message = "Debe haber al menos una imagen")
    private List<ProductImageRequest> images;
}
