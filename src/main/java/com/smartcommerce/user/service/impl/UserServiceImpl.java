package com.smartcommerce.user.service.impl;

import com.smartcommerce.exception.UserNotFoundException;
import com.smartcommerce.user.dto.request.UpdateUserRequest;
import com.smartcommerce.user.dto.response.UserResponseDTO;
import com.smartcommerce.user.entity.Role;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.mapper.UserMapper;
import com.smartcommerce.user.repository.UserRepository;
import com.smartcommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDTO getProfile(Long userId) {
        return userMapper.toDTO(getUserOrThrow(userId));
    }

    @Override
    public UserResponseDTO updateProfile(Long userId, UpdateUserRequest request) {
        User user = getUserOrThrow(userId);
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getBirthDate() != null) user.setBirthDate(request.getBirthDate());
        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDTO);
    }

    @Override
    public UserResponseDTO getUserById(Long userId) {
        return userMapper.toDTO(getUserOrThrow(userId));
    }

    @Override
    public void deactivateUser(Long userId) {
        User user = getUserOrThrow(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public void promoteToAdmin(Long userId) {
        User user = getUserOrThrow(userId);
        user.setRole(Role.ADMIN);
        userRepository.save(user);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
