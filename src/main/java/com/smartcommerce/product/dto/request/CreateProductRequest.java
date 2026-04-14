package com.smartcommerce.product.dto.request;

import com.smartcommerce.product.entity.ProductStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class CreateProductRequest {

    // Campo obligatorio y no puede estar vacío (ni espacios)
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    // Opcional
    private String description;

    // Campo obligatorio (no puede ser null)
    @NotNull(message = "El precio es obligatorio")
// Valor debe ser mayor que 0
    @Positive(message = "El precio debe ser mayor que 0")
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio")
// Valor mínimo permitido (incluye el valor indicado)
    @Min(value= 0, message="El stock no puede ser negativo")
    private Integer stock;

    private String brand;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    private List<String> imageUrls;
}

