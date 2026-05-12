package com.smartcommerce.auth.service;

import com.smartcommerce.auth.dto.request.LoginRequest;
import com.smartcommerce.auth.dto.request.RegisterRequest;
import com.smartcommerce.auth.dto.response.AuthResponse;
import com.smartcommerce.auth.service.impl.AuthServiceImpl;
import com.smartcommerce.exception.EmailAlreadyExistsException;
import com.smartcommerce.security.JwtService;
import com.smartcommerce.user.entity.Role;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;
    @InjectMocks AuthServiceImpl authService;

    @Test
    void register_withNewEmail_shouldReturnToken() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Juan");
        req.setLastName("Pérez");
        req.setPhone("600000000");
        req.setBirthDate(LocalDate.of(1990, 1, 1));
        req.setEmail("juan@test.com");
        req.setPassword("password123");

        when(userRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        User savedUser = User.builder().id(1L).email("juan@test.com").role(Role.USER).active(true).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.register(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("juan@test.com");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    void register_withExistingEmail_shouldThrowException() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("existing@test.com");
        req.setPassword("password123");

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        LoginRequest req = new LoginRequest();
        req.setEmail("juan@test.com");
        req.setPassword("password123");

        User user = User.builder().id(1L).email("juan@test.com").role(Role.USER).active(true).build();
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
