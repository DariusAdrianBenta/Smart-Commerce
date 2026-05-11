# Phase 04 — Address Module

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans`.

**Goal:** Implementar el módulo de direcciones de envío. Cada usuario puede tener varias direcciones; una es la dirección por defecto. Al marcar una como default, la anterior se desmarca automáticamente.

**Architecture:** `AddressController` → `AddressServiceImpl` → `AddressRepository`. La FK `user_id` relaciona `Address` con `User`.

**Pre-requisitos:** Phase 01 y 02 completadas (`User` entity y `UserRepository` existen).

---

## Archivos a crear

| Acción | Archivo |
|--------|---------|
| Crear | `src/main/java/com/smartcommerce/address/entity/Address.java` |
| Crear | `src/main/java/com/smartcommerce/address/repository/AddressRepository.java` |
| Crear | `src/main/java/com/smartcommerce/address/dto/request/AddressRequest.java` |
| Crear | `src/main/java/com/smartcommerce/address/dto/response/AddressResponseDTO.java` |
| Crear | `src/main/java/com/smartcommerce/address/mapper/AddressMapper.java` |
| Crear | `src/main/java/com/smartcommerce/address/service/AddressService.java` |
| Crear | `src/main/java/com/smartcommerce/address/service/impl/AddressServiceImpl.java` |
| Crear | `src/main/java/com/smartcommerce/address/controller/AddressController.java` |
| Modificar | `src/main/java/com/smartcommerce/exception/GlobalExceptionHandler.java` |
| Crear | `src/test/java/com/smartcommerce/address/service/AddressServiceTest.java` |

---

## Task 1: Entidad Address

- [ ] Crear `src/main/java/com/smartcommerce/address/entity/Address.java`:

```java
package com.smartcommerce.address.entity;

import com.smartcommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private boolean isDefault;
}
```

---

## Task 2: AddressRepository

- [ ] Crear `src/main/java/com/smartcommerce/address/repository/AddressRepository.java`:

```java
package com.smartcommerce.address.repository;

import com.smartcommerce.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultForUser(Long userId);
}
```

---

## Task 3: DTOs

- [ ] Crear `src/main/java/com/smartcommerce/address/dto/request/AddressRequest.java`:

```java
package com.smartcommerce.address.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {

    @NotBlank
    private String street;

    @NotBlank
    private String city;

    @NotBlank
    private String province;

    @NotBlank
    private String postalCode;

    @NotBlank
    private String country;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/address/dto/response/AddressResponseDTO.java`:

```java
package com.smartcommerce.address.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressResponseDTO {
    private Long id;
    private String street;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    private boolean isDefault;
}
```

---

## Task 4: AddressMapper

- [ ] Crear `src/main/java/com/smartcommerce/address/mapper/AddressMapper.java`:

```java
package com.smartcommerce.address.mapper;

import com.smartcommerce.address.dto.response.AddressResponseDTO;
import com.smartcommerce.address.entity.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressResponseDTO toDTO(Address address);
}
```

---

## Task 5: Excepción

- [ ] Crear `src/main/java/com/smartcommerce/exception/AddressNotFoundException.java`:

```java
package com.smartcommerce.exception;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(Long id) {
        super("Dirección no encontrada con id: " + id);
    }
}
```

- [ ] Añadir handler en `GlobalExceptionHandler.java`:

```java
@ExceptionHandler(AddressNotFoundException.class)
public ResponseEntity<ErrorResponse> handleAddressNotFound(AddressNotFoundException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}
```

---

## Task 6: AddressService

- [ ] Crear `src/main/java/com/smartcommerce/address/service/AddressService.java`:

```java
package com.smartcommerce.address.service;

import com.smartcommerce.address.dto.request.AddressRequest;
import com.smartcommerce.address.dto.response.AddressResponseDTO;

import java.util.List;

public interface AddressService {
    List<AddressResponseDTO> getMyAddresses(Long userId);
    AddressResponseDTO addAddress(Long userId, AddressRequest request);
    AddressResponseDTO updateAddress(Long userId, Long addressId, AddressRequest request);
    void deleteAddress(Long userId, Long addressId);
    void setDefault(Long userId, Long addressId);
}
```

---

## Task 7: AddressServiceImpl

- [ ] **Escribir el test primero** `src/test/java/com/smartcommerce/address/service/AddressServiceTest.java`:

```java
package com.smartcommerce.address.service;

import com.smartcommerce.address.dto.request.AddressRequest;
import com.smartcommerce.address.dto.response.AddressResponseDTO;
import com.smartcommerce.address.entity.Address;
import com.smartcommerce.address.mapper.AddressMapper;
import com.smartcommerce.address.repository.AddressRepository;
import com.smartcommerce.address.service.impl.AddressServiceImpl;
import com.smartcommerce.exception.AddressNotFoundException;
import com.smartcommerce.user.entity.Role;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock AddressRepository addressRepository;
    @Mock UserRepository userRepository;
    @Mock AddressMapper addressMapper;
    @InjectMocks AddressServiceImpl addressService;

    private User mockUser(Long id) {
        return User.builder().id(id).email("u@test.com").role(Role.USER).active(true).build();
    }

    @Test
    void getMyAddresses_returnsListForUser() {
        Address addr = Address.builder().id(1L).city("Madrid").build();
        AddressResponseDTO dto = AddressResponseDTO.builder().id(1L).city("Madrid").build();
        when(addressRepository.findByUserId(1L)).thenReturn(List.of(addr));
        when(addressMapper.toDTO(addr)).thenReturn(dto);

        List<AddressResponseDTO> result = addressService.getMyAddresses(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCity()).isEqualTo("Madrid");
    }

    @Test
    void addAddress_savesAndReturnsDTO() {
        User user = mockUser(1L);
        AddressRequest req = new AddressRequest();
        req.setStreet("Calle Mayor 1"); req.setCity("Madrid");
        req.setProvince("Madrid"); req.setPostalCode("28001"); req.setCountry("España");

        Address saved = Address.builder().id(1L).city("Madrid").build();
        AddressResponseDTO dto = AddressResponseDTO.builder().id(1L).city("Madrid").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.save(any(Address.class))).thenReturn(saved);
        when(addressMapper.toDTO(saved)).thenReturn(dto);

        AddressResponseDTO result = addressService.addAddress(1L, req);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void setDefault_clearsOldDefaultAndSetsNew() {
        User user = mockUser(1L);
        Address address = Address.builder().id(2L).user(user).isDefault(false).build();
        when(addressRepository.findById(2L)).thenReturn(Optional.of(address));

        addressService.setDefault(1L, 2L);

        verify(addressRepository).clearDefaultForUser(1L);
        assertThat(address.isDefault()).isTrue();
        verify(addressRepository).save(address);
    }

    @Test
    void deleteAddress_whenNotOwner_throwsException() {
        Address address = Address.builder().id(1L)
                .user(User.builder().id(99L).build()).build();
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> addressService.deleteAddress(1L, 1L))
                .isInstanceOf(AddressNotFoundException.class);
    }
}
```

- [ ] Ejecutar el test para verificar que falla:

```bash
./mvnw test -Dtest=AddressServiceTest
```

- [ ] Implementar `src/main/java/com/smartcommerce/address/service/impl/AddressServiceImpl.java`:

```java
package com.smartcommerce.address.service.impl;

import com.smartcommerce.address.dto.request.AddressRequest;
import com.smartcommerce.address.dto.response.AddressResponseDTO;
import com.smartcommerce.address.entity.Address;
import com.smartcommerce.address.mapper.AddressMapper;
import com.smartcommerce.address.repository.AddressRepository;
import com.smartcommerce.address.service.AddressService;
import com.smartcommerce.exception.AddressNotFoundException;
import com.smartcommerce.exception.UserNotFoundException;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    public List<AddressResponseDTO> getMyAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(addressMapper::toDTO)
                .toList();
    }

    @Override
    public AddressResponseDTO addAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Address address = Address.builder()
                .user(user)
                .street(request.getStreet())
                .city(request.getCity())
                .province(request.getProvince())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(false)
                .build();

        return addressMapper.toDTO(addressRepository.save(address));
    }

    @Override
    public AddressResponseDTO updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = getAddressOwnedByUser(userId, addressId);
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setProvince(request.getProvince());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        return addressMapper.toDTO(addressRepository.save(address));
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        Address address = getAddressOwnedByUser(userId, addressId);
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long addressId) {
        Address address = getAddressOwnedByUser(userId, addressId);
        addressRepository.clearDefaultForUser(userId);
        address.setDefault(true);
        addressRepository.save(address);
    }

    private Address getAddressOwnedByUser(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        if (!address.getUser().getId().equals(userId)) {
            throw new AddressNotFoundException(addressId);
        }
        return address;
    }
}
```

- [ ] Ejecutar el test:

```bash
./mvnw test -Dtest=AddressServiceTest
```

Resultado esperado: `Tests run: 4, Failures: 0, Errors: 0`

---

## Task 8: AddressController

- [ ] Crear `src/main/java/com/smartcommerce/address/controller/AddressController.java`:

```java
package com.smartcommerce.address.controller;

import com.smartcommerce.address.dto.request.AddressRequest;
import com.smartcommerce.address.dto.response.AddressResponseDTO;
import com.smartcommerce.address.service.AddressService;
import com.smartcommerce.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getMyAddresses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(addressService.getMyAddresses(user.getId()));
    }

    @PostMapping
    public ResponseEntity<AddressResponseDTO> addAddress(
            Authentication authentication,
            @Valid @RequestBody AddressRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.addAddress(user.getId(), request));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            Authentication authentication,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(addressService.updateAddress(user.getId(), addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            Authentication authentication,
            @PathVariable Long addressId) {
        User user = (User) authentication.getPrincipal();
        addressService.deleteAddress(user.getId(), addressId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefault(
            Authentication authentication,
            @PathVariable Long addressId) {
        User user = (User) authentication.getPrincipal();
        addressService.setDefault(user.getId(), addressId);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Task 9: Verificar y commit

- [ ] Ejecutar todos los tests:

```bash
./mvnw test
```

Resultado esperado: `BUILD SUCCESS`

- [ ] Commit:

```bash
git add src/main/java/com/smartcommerce/address/ \
        src/main/java/com/smartcommerce/exception/ \
        src/test/java/com/smartcommerce/address/
git commit -m "feat(address): add multi-address management with default address support"
```
