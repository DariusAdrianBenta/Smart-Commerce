package com.smartcommerce.user.mapper;

import com.smartcommerce.user.dto.response.UserResponseDTO;
import com.smartcommerce.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role", target = "role", qualifiedByName = "roleToString")
    UserResponseDTO toDTO(User user);

    @org.mapstruct.Named("roleToString")
    default String roleToString(com.smartcommerce.user.entity.Role role) {
        return role != null ? role.name() : null;
    }
}
