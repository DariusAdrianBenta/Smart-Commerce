# Phase 10 — Wishlist Module

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans`.

**Goal:** Cada usuario autenticado puede crear múltiples listas de deseos con nombre personalizado (ej: "Navidad", "Para mí") y añadir/eliminar productos de cada lista.

**Architecture:** `WishlistController` → `WishlistServiceImpl` → `WishlistRepository`. Relación ManyToMany entre `Wishlist` y `Product` via tabla `wishlist_products`.

**Pre-requisitos:** Phase 01 y 02 completadas. Módulo `product` (entity `Product`) ya existe.

---

## Archivos a crear

| Acción | Archivo |
|--------|---------|
| Crear | `src/main/java/com/smartcommerce/wishlist/entity/Wishlist.java` |
| Crear | `src/main/java/com/smartcommerce/wishlist/repository/WishlistRepository.java` |
| Crear | `src/main/java/com/smartcommerce/wishlist/dto/request/WishlistRequest.java` |
| Crear | `src/main/java/com/smartcommerce/wishlist/dto/response/WishlistResponseDTO.java` |
| Crear | `src/main/java/com/smartcommerce/wishlist/mapper/WishlistMapper.java` |
| Crear | `src/main/java/com/smartcommerce/wishlist/service/WishlistService.java` |
| Crear | `src/main/java/com/smartcommerce/wishlist/service/impl/WishlistServiceImpl.java` |
| Crear | `src/main/java/com/smartcommerce/wishlist/controller/WishlistController.java` |
| Modificar | `src/main/java/com/smartcommerce/exception/GlobalExceptionHandler.java` |
| Crear | `src/test/java/com/smartcommerce/wishlist/service/WishlistServiceTest.java` |

---

## Task 1: Entidad Wishlist

- [ ] Crear `src/main/java/com/smartcommerce/wishlist/entity/Wishlist.java`:

```java
package com.smartcommerce.wishlist.entity;

import com.smartcommerce.product.entity.Product;
import com.smartcommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "wishlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(
        name = "wishlist_products",
        joinColumns = @JoinColumn(name = "wishlist_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @Builder.Default
    private Set<Product> products = new HashSet<>();

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
```

---

## Task 2: WishlistRepository

- [ ] Crear `src/main/java/com/smartcommerce/wishlist/repository/WishlistRepository.java`:

```java
package com.smartcommerce.wishlist.repository;

import com.smartcommerce.wishlist.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserId(Long userId);
    Optional<Wishlist> findByIdAndUserId(Long id, Long userId);
}
```

---

## Task 3: DTOs

- [ ] Crear `src/main/java/com/smartcommerce/wishlist/dto/request/WishlistRequest.java`:

```java
package com.smartcommerce.wishlist.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishlistRequest {

    @NotBlank
    private String name;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/wishlist/dto/response/WishlistResponseDTO.java`:

```java
package com.smartcommerce.wishlist.dto.response;

import com.smartcommerce.product.dto.response.ProductResponseDTO;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class WishlistResponseDTO {
    private Long id;
    private String name;
    private List<ProductResponseDTO> products;
    private LocalDateTime createdAt;
}
```

---

## Task 4: WishlistMapper

- [ ] Crear `src/main/java/com/smartcommerce/wishlist/mapper/WishlistMapper.java`:

```java
package com.smartcommerce.wishlist.mapper;

import com.smartcommerce.product.mapper.ProductMapper;
import com.smartcommerce.wishlist.dto.response.WishlistResponseDTO;
import com.smartcommerce.wishlist.entity.Wishlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface WishlistMapper {

    @Mapping(source = "products", target = "products")
    WishlistResponseDTO toDTO(Wishlist wishlist);
}
```

---

## Task 5: Excepción

- [ ] Crear `src/main/java/com/smartcommerce/exception/WishlistNotFoundException.java`:

```java
package com.smartcommerce.exception;

public class WishlistNotFoundException extends RuntimeException {
    public WishlistNotFoundException(Long id) {
        super("Lista de deseos no encontrada con id: " + id);
    }
}
```

- [ ] Añadir handler en `GlobalExceptionHandler.java`:

```java
@ExceptionHandler(WishlistNotFoundException.class)
public ResponseEntity<ErrorResponse> handleWishlistNotFound(WishlistNotFoundException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}
```

---

## Task 6: WishlistService

- [ ] Crear `src/main/java/com/smartcommerce/wishlist/service/WishlistService.java`:

```java
package com.smartcommerce.wishlist.service;

import com.smartcommerce.wishlist.dto.request.WishlistRequest;
import com.smartcommerce.wishlist.dto.response.WishlistResponseDTO;

import java.util.List;

public interface WishlistService {
    List<WishlistResponseDTO> getMyWishlists(Long userId);
    WishlistResponseDTO createWishlist(Long userId, WishlistRequest request);
    WishlistResponseDTO renameWishlist(Long userId, Long wishlistId, WishlistRequest request);
    void deleteWishlist(Long userId, Long wishlistId);
    WishlistResponseDTO addProduct(Long userId, Long wishlistId, Long productId);
    WishlistResponseDTO removeProduct(Long userId, Long wishlistId, Long productId);
}
```

---

## Task 7: WishlistServiceImpl

- [ ] **Escribir el test primero** `src/test/java/com/smartcommerce/wishlist/service/WishlistServiceTest.java`:

```java
package com.smartcommerce.wishlist.service;

import com.smartcommerce.exception.WishlistNotFoundException;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.entity.ProductStatus;
import com.smartcommerce.product.repository.ProductRepository;
import com.smartcommerce.user.entity.Role;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import com.smartcommerce.wishlist.dto.request.WishlistRequest;
import com.smartcommerce.wishlist.entity.Wishlist;
import com.smartcommerce.wishlist.mapper.WishlistMapper;
import com.smartcommerce.wishlist.repository.WishlistRepository;
import com.smartcommerce.wishlist.service.impl.WishlistServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock WishlistRepository wishlistRepository;
    @Mock UserRepository userRepository;
    @Mock ProductRepository productRepository;
    @Mock WishlistMapper wishlistMapper;
    @InjectMocks WishlistServiceImpl wishlistService;

    private User mockUser(Long id) {
        return User.builder().id(id).email("u@test.com").role(Role.USER).active(true).build();
    }

    private Product mockProduct(Long id) {
        return Product.builder().id(id).name("Prod").price(BigDecimal.TEN)
                .stock(5).status(ProductStatus.ACTIVE).build();
    }

    @Test
    void createWishlist_savesWithCorrectName() {
        User user = mockUser(1L);
        WishlistRequest req = new WishlistRequest();
        req.setName("Navidad");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(wishlistRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(wishlistMapper.toDTO(any())).thenReturn(
                com.smartcommerce.wishlist.dto.response.WishlistResponseDTO.builder()
                        .id(1L).name("Navidad").build());

        var result = wishlistService.createWishlist(1L, req);

        assertThat(result.getName()).isEqualTo("Navidad");
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void addProduct_addsToWishlistProducts() {
        User user = mockUser(1L);
        Wishlist wishlist = Wishlist.builder().id(1L).user(user)
                .name("Test").products(new HashSet<>()).build();
        Product product = mockProduct(10L);

        when(wishlistRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(wishlist));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(wishlistRepository.save(any())).thenReturn(wishlist);
        when(wishlistMapper.toDTO(any())).thenReturn(
                com.smartcommerce.wishlist.dto.response.WishlistResponseDTO.builder()
                        .id(1L).name("Test").build());

        wishlistService.addProduct(1L, 1L, 10L);

        assertThat(wishlist.getProducts()).contains(product);
        verify(wishlistRepository).save(wishlist);
    }

    @Test
    void deleteWishlist_fromAnotherUser_throwsException() {
        when(wishlistRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.deleteWishlist(2L, 1L))
                .isInstanceOf(WishlistNotFoundException.class);
    }
}
```

- [ ] Ejecutar test para verificar que falla:

```bash
./mvnw test -Dtest=WishlistServiceTest
```

- [ ] Implementar `src/main/java/com/smartcommerce/wishlist/service/impl/WishlistServiceImpl.java`:

```java
package com.smartcommerce.wishlist.service.impl;

import com.smartcommerce.exception.ProductNotFoundException;
import com.smartcommerce.exception.UserNotFoundException;
import com.smartcommerce.exception.WishlistNotFoundException;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.repository.ProductRepository;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import com.smartcommerce.wishlist.dto.request.WishlistRequest;
import com.smartcommerce.wishlist.dto.response.WishlistResponseDTO;
import com.smartcommerce.wishlist.entity.Wishlist;
import com.smartcommerce.wishlist.mapper.WishlistMapper;
import com.smartcommerce.wishlist.repository.WishlistRepository;
import com.smartcommerce.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WishlistMapper wishlistMapper;

    @Override
    public List<WishlistResponseDTO> getMyWishlists(Long userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(wishlistMapper::toDTO)
                .toList();
    }

    @Override
    public WishlistResponseDTO createWishlist(Long userId, WishlistRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .name(request.getName())
                .build();
        return wishlistMapper.toDTO(wishlistRepository.save(wishlist));
    }

    @Override
    public WishlistResponseDTO renameWishlist(Long userId, Long wishlistId, WishlistRequest request) {
        Wishlist wishlist = getWishlistOwnedByUser(userId, wishlistId);
        wishlist.setName(request.getName());
        return wishlistMapper.toDTO(wishlistRepository.save(wishlist));
    }

    @Override
    public void deleteWishlist(Long userId, Long wishlistId) {
        Wishlist wishlist = getWishlistOwnedByUser(userId, wishlistId);
        wishlistRepository.delete(wishlist);
    }

    @Override
    @Transactional
    public WishlistResponseDTO addProduct(Long userId, Long wishlistId, Long productId) {
        Wishlist wishlist = getWishlistOwnedByUser(userId, wishlistId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        wishlist.getProducts().add(product);
        return wishlistMapper.toDTO(wishlistRepository.save(wishlist));
    }

    @Override
    @Transactional
    public WishlistResponseDTO removeProduct(Long userId, Long wishlistId, Long productId) {
        Wishlist wishlist = getWishlistOwnedByUser(userId, wishlistId);
        wishlist.getProducts().removeIf(p -> p.getId().equals(productId));
        return wishlistMapper.toDTO(wishlistRepository.save(wishlist));
    }

    private Wishlist getWishlistOwnedByUser(Long userId, Long wishlistId) {
        return wishlistRepository.findByIdAndUserId(wishlistId, userId)
                .orElseThrow(() -> new WishlistNotFoundException(wishlistId));
    }
}
```

- [ ] Ejecutar el test:

```bash
./mvnw test -Dtest=WishlistServiceTest
```

Resultado esperado: `Tests run: 3, Failures: 0, Errors: 0`

---

## Task 8: WishlistController

- [ ] Crear `src/main/java/com/smartcommerce/wishlist/controller/WishlistController.java`:

```java
package com.smartcommerce.wishlist.controller;

import com.smartcommerce.user.entity.User;
import com.smartcommerce.wishlist.dto.request.WishlistRequest;
import com.smartcommerce.wishlist.dto.response.WishlistResponseDTO;
import com.smartcommerce.wishlist.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<WishlistResponseDTO>> getMyWishlists(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(wishlistService.getMyWishlists(user.getId()));
    }

    @PostMapping
    public ResponseEntity<WishlistResponseDTO> createWishlist(
            Authentication authentication,
            @Valid @RequestBody WishlistRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(wishlistService.createWishlist(user.getId(), request));
    }

    @PutMapping("/{wishlistId}")
    public ResponseEntity<WishlistResponseDTO> renameWishlist(
            Authentication authentication,
            @PathVariable Long wishlistId,
            @Valid @RequestBody WishlistRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(wishlistService.renameWishlist(user.getId(), wishlistId, request));
    }

    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<Void> deleteWishlist(
            Authentication authentication,
            @PathVariable Long wishlistId) {
        User user = (User) authentication.getPrincipal();
        wishlistService.deleteWishlist(user.getId(), wishlistId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{wishlistId}/products/{productId}")
    public ResponseEntity<WishlistResponseDTO> addProduct(
            Authentication authentication,
            @PathVariable Long wishlistId,
            @PathVariable Long productId) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(wishlistService.addProduct(user.getId(), wishlistId, productId));
    }

    @DeleteMapping("/{wishlistId}/products/{productId}")
    public ResponseEntity<WishlistResponseDTO> removeProduct(
            Authentication authentication,
            @PathVariable Long wishlistId,
            @PathVariable Long productId) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(wishlistService.removeProduct(user.getId(), wishlistId, productId));
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
git add src/main/java/com/smartcommerce/wishlist/ \
        src/main/java/com/smartcommerce/exception/ \
        src/test/java/com/smartcommerce/wishlist/
git commit -m "feat(wishlist): add multiple wishlists per user with ManyToMany product relation"
```
