package com.smartcommerce.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductImageRequest {

    @NotBlank(message = "La URL de la imagen es obligatoria")
    private String imageUrl;
}
