package com.smartcommerce.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class UpdateProductRequest {
// Aqui todo es opcional, nada tiene que ser obligatorio
// Ponemos validaciones para evitar datos incorrectos


    private String name;

    private String description;

    @Positive(message = "El precio debe ser mayor que 0")
    private BigDecimal price;

    @Min(value= 0, message="El stock no puede ser negativo")
    private Integer stock;

    private String brand;

    private Long categoryId;

    private List<String> imageUrls;

}
