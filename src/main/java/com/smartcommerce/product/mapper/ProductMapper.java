package com.smartcommerce.product.mapper;

import com.smartcommerce.product.dto.request.CreateProductRequest;
import com.smartcommerce.product.dto.request.UpdateProductRequest;
import com.smartcommerce.product.dto.response.ProductResponseDTO;
import com.smartcommerce.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


//TODO Quitar el ignore de Category cuando :
//TODO Relación activa en entity
//TODO Repository de Category
//TODO Lógica en service





// MapStruct Genera automáticamente la clase ProductMapperImpl
// y Spring se encarga de inyectarlo, evitando usar 'new'
@Mapper(componentModel="spring")
public interface ProductMapper {
    // DTO → ENTITY
    // Indica lo que no queremos mapear
    // Convierte CreateProductRequest -> Product
    // Mapea automáticamente los campos iguales
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    Product toEntity(CreateProductRequest dto);


    // ENTITY → DTO
    // Convierte Product -> ProductResponseDTO
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "imageUrls", ignore = true)
    ProductResponseDTO toDTO(Product product);


    // DTO → ENTITY (actualiza el existente)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    void updateProductFromDto(UpdateProductRequest dto, @MappingTarget Product product);





}
