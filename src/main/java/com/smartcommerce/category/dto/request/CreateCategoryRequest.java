package com.smartcommerce.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private Long parentId;

}
