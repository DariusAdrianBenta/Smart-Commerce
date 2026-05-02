package com.smartcommerce.category.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class UpdateCategoryRequest {

// Uso @Size en lugar de @NotBlank para permitir updates flexibles.
// Aunque el frontend envía todos los campos, así evitamos problemas
// si en el futuro solo se quiere actualizar uno de ellos.

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    private Long parentId;
}
