# Phase 02 — Auth Module

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans`.

**Goal:** Implementar los endpoints de registro y login. El registro crea un usuario con rol USER. El login valida credenciales y devuelve un JWT de 1 hora.

**Architecture:** `AuthController` → `AuthServiceImpl` → `UserRepository` + `JwtService` + `PasswordEncoder`. Sin lógica de refresh token.

**Tech Stack:** Spring Security 7 · jjwt 0.12.6 · BCrypt · MapStruct · Jakarta Validation

**Pre-requisitos:** Phase 01 completada (`User`, `UserRepository`, `JwtService`, `SecurityConfig` existen).

---

## Archivos a crear

| Acción | Archivo |
|--------|---------|
| Crear | `src/main/java/com/smartcommerce/auth/dto/request/RegisterRequest.java` |
| Crear | `src/main/java/com/smartcommerce/auth/dto/request/LoginRequest.java` |
| Crear | `src/main/java/com/smartcommerce/auth/dto/response/AuthResponse.java` |
| Crear | `src/main/java/com/smartcommerce/auth/service/AuthService.java` |
| Crear | `src/main/java/com/smartcommerce/auth/service/impl/AuthServiceImpl.java` |
| Crear | `src/main/java/com/smartcommerce/auth/controller/AuthController.java` |
| Modificar | `src/main/java/com/smartcommerce/exception/GlobalExceptionHandler.java` |
| Crear | `src/test/java/com/smartcommerce/auth/service/AuthServiceTest.java` |

---

## Task 1: DTOs de request

- [ ] Crear `src/main/java/com/smartcommerce/auth/dto/request/RegisterRequest.java`:

```java
package com.smartcommerce.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phone;

    @NotNull
    @Past
    private LocalDate birthDate;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/auth/dto/request/LoginRequest.java`:

```java
package com.smartcommerce.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
```

---

## Task 2: DTO de response

- [ ] Crear `src/main/java/com/smartcommerce/auth/dto/response/AuthResponse.java`:

```java
package com.smartcommerce.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private long expiresIn;
    private Long userId;
    private String email;
    private String role;
}
```

---

## Task 3: Excepciones de auth

- [ ] Crear `src/main/java/com/smartcommerce/exception/EmailAlreadyExistsException.java`:

```java
package com.smartcommerce.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("El email ya está registrado: " + email);
    }
}
```

- [ ] Añadir el handler en `GlobalExceptionHandler.java`:

```java
@ExceptionHandler(EmailAlreadyExistsException.class)
public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .message(ex.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
}

@ExceptionHandler(BadCredentialsException.class)
public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.UNAUTHORIZED.value())
            .message("Credenciales incorrectas")
            .build();
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
}

@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(java.util.stream.Collectors.joining(", "));
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .message(message)
            .build();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
```

> Añadir imports necesarios en `GlobalExceptionHandler`:
> `import org.springframework.security.authentication.BadCredentialsException;`
> `import org.springframework.web.bind.MethodArgumentNotValidException;`

---

## Task 4: AuthService

- [ ] Crear `src/main/java/com/smartcommerce/auth/service/AuthService.java`:

```java
package com.smartcommerce.auth.service;

import com.smartcommerce.auth.dto.request.LoginRequest;
import com.smartcommerce.auth.dto.request.RegisterRequest;
import com.smartcommerce.auth.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
```

---

## Task 5: AuthServiceImpl

- [ ] **Escribir el test primero** `src/test/java/com/smartcommerce/auth/service/AuthServiceTest.java`:

```java
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
```

- [ ] Ejecutar el test para verificar que falla:

```bash
./mvnw test -Dtest=AuthServiceTest
```

Resultado esperado: `COMPILATION ERROR` o `ClassNotFoundException` (aún no existe `AuthServiceImpl`)

- [ ] Implementar `src/main/java/com/smartcommerce/auth/service/impl/AuthServiceImpl.java`:

```java
package com.smartcommerce.auth.service.impl;

import com.smartcommerce.auth.dto.request.LoginRequest;
import com.smartcommerce.auth.dto.request.RegisterRequest;
import com.smartcommerce.auth.dto.response.AuthResponse;
import com.smartcommerce.auth.service.AuthService;
import com.smartcommerce.exception.EmailAlreadyExistsException;
import com.smartcommerce.security.JwtService;
import com.smartcommerce.user.entity.Role;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .birthDate(request.getBirthDate())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);

        return AuthResponse.builder()
                .token(token)
                .expiresIn(3600)
                .userId(saved.getId())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .expiresIn(3600)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
```

- [ ] Ejecutar el test:

```bash
./mvnw test -Dtest=AuthServiceTest
```

Resultado esperado: `Tests run: 3, Failures: 0, Errors: 0`

---

## Task 6: AuthController

- [ ] Crear `src/main/java/com/smartcommerce/auth/controller/AuthController.java`:

```java
package com.smartcommerce.auth.controller;

import com.smartcommerce.auth.dto.request.LoginRequest;
import com.smartcommerce.auth.dto.request.RegisterRequest;
import com.smartcommerce.auth.dto.response.AuthResponse;
import com.smartcommerce.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

---

## Task 7: Verificar manualmente con curl

- [ ] Probar registro:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Juan",
    "lastName": "Perez",
    "phone": "600000000",
    "birthDate": "1990-01-01",
    "email": "juan@test.com",
    "password": "password123"
  }'
```

Resultado esperado: `201 Created` con `{ "token": "eyJ...", "expiresIn": 3600, ... }`

- [ ] Probar login:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "email": "admin@smartcommerce.com", "password": "Admin1234!" }'
```

Resultado esperado: `200 OK` con token JWT y `"role": "ADMIN"`

- [ ] Commit:

```bash
git add src/main/java/com/smartcommerce/auth/ \
        src/main/java/com/smartcommerce/exception/ \
        src/test/java/com/smartcommerce/auth/
git commit -m "feat(auth): add register and login endpoints with JWT response"
```
