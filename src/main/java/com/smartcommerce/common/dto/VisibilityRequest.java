package com.smartcommerce.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Cuerpo de las peticiones PATCH .../visibility. visible=false oculta, true muestra.
@Data
public class VisibilityRequest {

    @NotNull(message = "El campo 'visible' es obligatorio")
    private Boolean visible;
}
