package com.smartcommerce.user.service;

import com.smartcommerce.exception.UserNotFoundException;
import com.smartcommerce.user.dto.request.UpdateUserRequest;
import com.smartcommerce.user.dto.response.UserResponseDTO;
import com.smartcommerce.user.entity.Role;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.mapper.UserMapper;
import com.smartcommerce.user.repository.UserRepository;
import com.smartcommerce.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserMapper userMapper;
    @InjectMocks UserServiceImpl userService;

    private User buildUser(Long id, Role role) {
        return User.builder().id(id).email("u@test.com").role(role).active(true).build();
    }

    @Test
    void getProfile_existingUser_returnsDTO() {
        User user = buildUser(1L, Role.USER);
        UserResponseDTO dto = UserResponseDTO.builder().id(1L).email("u@test.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(dto);

        UserResponseDTO result = userService.getProfile(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getProfile_nonExistingUser_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getProfile(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deactivateUser_setsActiveToFalse() {
        User user = buildUser(1L, Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.deactivateUser(1L);

        assertThat(user.isActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void promoteToAdmin_setsRoleToAdmin() {
        User user = buildUser(1L, Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.promoteToAdmin(1L);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).save(user);
    }
}
