package com.smartcommerce.address.mapper;

import com.smartcommerce.address.dto.response.AddressResponseDTO;
import com.smartcommerce.address.entity.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressResponseDTO toDTO(Address address);
}
