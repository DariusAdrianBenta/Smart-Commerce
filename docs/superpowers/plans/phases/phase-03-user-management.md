# Phase 03 — User Management

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans`.

**Goal:** Exponer endpoints para que el usuario vea y edite su propio perfil, y para que el ADMIN gestione todos los usuarios (listar, desactivar, promover a ADMIN).

**Architecture:** `UserController` → `UserServiceImpl` → `UserRepository`. MapStruct para User → UserResponseDTO.

**Pre-requisitos:** Phase 01 y 02 completadas.

---

## Archivos a crear

| Acción | Archivo |
|--------|---------|
| Crear | `src/main/java/com/smartcommerce/user/dto/response/UserResponseDTO.java` |
| Crear | `src/main/java/com/smartcommerce/user/dto/request/UpdateUserRequest.java` |
| Crear | `src/main/java/com/smartcommerce/user/mapper/UserMapper.java` |
| Crear | `src/main/java/com/smartcommerce/user/service/UserService.java` |
| Crear | `src/main/java/com/smartcommerce/user/service/impl/UserServiceImpl.java` |
| Crear | `src/main/java/com/smartcommerce/user/controller/UserController.java` |
| Modificar | `src/main/java/com/smartcommerce/exception/GlobalExceptionHandler.java` |
| Crear | `src/test/java/com/smartcommerce/user/service/UserServiceTest.java` |

---

## Task 1: DTOs

- [ ] Crear `src/main/java/com/smartcommerce/user/dto/response/UserResponseDTO.java`:

```java
package com.smartcommerce.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate birthDate;
    private String email;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/user/dto/request/UpdateUserRequest.java`:

```java
package com.smartcommerce.user.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String phone;

    @Past
    private LocalDate birthDate;
}
```

---

## Task 2: Excepción de usuario no encontrado

- [ ] Crear `src/main/java/com/smartcommerce/exception/UserNotFoundException.java`:

```java
package com.smartcommerce.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Usuario no encontrado con id: " + id);
    }
}
```

- [ ] Añadir handler en `GlobalExceptionHandler.java`:

```java
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}
```

---

## Task 3: UserMapper

- [ ] Crear `src/main/java/com/smartcommerce/user/mapper/UserMapper.java`:

```java
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
```

---

## Task 4: UserService

- [ ] Crear `src/main/java/com/smartcommerce/user/service/UserService.java`:

```java
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
```

---

## Task 5: UserServiceImpl

- [ ] **Escribir el test primero** `src/test/java/com/smartcommerce/user/service/UserServiceTest.java`:

```java
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
```

- [ ] Ejecutar el test para verificar que falla:

```bash
./mvnw test -Dtest=UserServiceTest
```

- [ ] Implementar `src/main/java/com/smartcommerce/user/service/impl/UserServiceImpl.java`:

```java
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
```

- [ ] Ejecutar el test:

```bash
./mvnw test -Dtest=UserServiceTest
```

Resultado esperado: `Tests run: 4, Failures: 0, Errors: 0`

---

## Task 6: UserController

- [ ] Crear `src/main/java/com/smartcommerce/user/controller/UserController.java`:

```java
package com.smartcommerce.user.controller;

import com.smartcommerce.user.dto.request.UpdateUserRequest;
import com.smartcommerce.user.dto.response.UserResponseDTO;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/me")
    public ResponseEntity<UserResponseDTO> getMyProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getProfile(user.getId()));
    }

    @PutMapping("/users/me")
    public ResponseEntity<UserResponseDTO> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateProfile(user.getId(), request));
    }

    // Admin endpoints
    @GetMapping("/admin/users")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/admin/users/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PatchMapping("/admin/users/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/users/{userId}/promote")
    public ResponseEntity<Void> promoteToAdmin(@PathVariable Long userId) {
        userService.promoteToAdmin(userId);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Task 7: Verificar y commit

- [ ] Ejecutar todos los tests:

```bash
./mvnw test
```

Resultado esperado: `BUILD SUCCESS`

- [ ] Commit:

```bash
git add src/main/java/com/smartcommerce/user/ \
        src/main/java/com/smartcommerce/exception/ \
        src/test/java/com/smartcommerce/user/
git commit -m "feat(user): add user profile and admin user management endpoints"
```
