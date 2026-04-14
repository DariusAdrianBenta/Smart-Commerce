package com.smartcommerce.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


// TODO Mover a la carpeta /productimage/dto/request/ cuando tenga ya la entidad ProductImage
@Data
public class ProductImageRequest {

    // Opcional → si es null, es una imagen nueva
    private Long id;


    @NotBlank(message = "La URL de la imagen es obligatoria")
    private String imageUrl;


    private Boolean isPrimary;


    @Min(value = 0, message = "La posición no puede ser negativa")
    private Integer position;
}
