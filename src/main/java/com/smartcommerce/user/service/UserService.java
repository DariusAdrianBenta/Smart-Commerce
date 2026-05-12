package com.smartcommerce.user.service;

import com.smartcommerce.user.dto.request.UpdateUserRequest;
import com.smartcommerce.user.dto.response.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponseDTO getProfile(Long userId);
    UserResponseDTO updateProfile(Long userId, UpdateUserRequest request);
    Page<UserResponseDTO> getAllUsers(Pageable pageable);
    UserResponseDTO getUserById(Long userId);
    void deactivateUser(Long userId);
    void promoteToAdmin(Long userId);
}
