package com.smartcommerce.category.dto.response;

import lombok.Data;

@Data
public class CategoryResponseDTO {
    private Long id;

    private String name;

    private String slug;

    private Long parentId;

    private String parentName;

}
