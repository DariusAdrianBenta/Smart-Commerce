# Phase 05 — Cart Module

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans`.

**Goal:** Implementar el carrito de compra persistente. Cada usuario autenticado tiene exactamente un carrito en base de datos. El carrito del usuario anónimo es responsabilidad del frontend (localStorage); al registrarse, el frontend llama a `/api/v1/cart/merge` con los items y el backend los fusiona.

**Architecture:** `Cart` (OneToOne con User) → `CartItem` (ManyToOne con Cart y Product). El carrito se crea lazily: se instancia la primera vez que el usuario añade un producto. Validaciones de stock en cada operación de escritura.

**Pre-requisitos:** Phase 01 y 02 completadas. El módulo `product` (entity `Product`, repository `ProductRepository`) ya existe.

---

## Archivos a crear

| Acción | Archivo |
|--------|---------|
| Crear | `src/main/java/com/smartcommerce/cart/entity/Cart.java` |
| Crear | `src/main/java/com/smartcommerce/cart/entity/CartItem.java` |
| Crear | `src/main/java/com/smartcommerce/cart/repository/CartRepository.java` |
| Crear | `src/main/java/com/smartcommerce/cart/repository/CartItemRepository.java` |
| Crear | `src/main/java/com/smartcommerce/cart/dto/request/AddCartItemRequest.java` |
| Crear | `src/main/java/com/smartcommerce/cart/dto/request/UpdateCartItemRequest.java` |
| Crear | `src/main/java/com/smartcommerce/cart/dto/request/MergeCartRequest.java` |
| Crear | `src/main/java/com/smartcommerce/cart/dto/response/CartItemResponseDTO.java` |
| Crear | `src/main/java/com/smartcommerce/cart/dto/response/CartResponseDTO.java` |
| Crear | `src/main/java/com/smartcommerce/cart/mapper/CartMapper.java` |
| Crear | `src/main/java/com/smartcommerce/cart/service/CartService.java` |
| Crear | `src/main/java/com/smartcommerce/cart/service/impl/CartServiceImpl.java` |
| Crear | `src/main/java/com/smartcommerce/cart/controller/CartController.java` |
| Modificar | `src/main/java/com/smartcommerce/exception/GlobalExceptionHandler.java` |
| Crear | `src/test/java/com/smartcommerce/cart/service/CartServiceTest.java` |

---

## Task 1: Entidades

- [ ] Crear `src/main/java/com/smartcommerce/cart/entity/Cart.java`:

```java
package com.smartcommerce.cart.entity;

import com.smartcommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
```

- [ ] Crear `src/main/java/com/smartcommerce/cart/entity/CartItem.java`:

```java
package com.smartcommerce.cart.entity;

import com.smartcommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;
}
```

---

## Task 2: Repositories

- [ ] Crear `src/main/java/com/smartcommerce/cart/repository/CartRepository.java`:

```java
package com.smartcommerce.cart.repository;

import com.smartcommerce.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
}
```

- [ ] Crear `src/main/java/com/smartcommerce/cart/repository/CartItemRepository.java`:

```java
package com.smartcommerce.cart.repository;

import com.smartcommerce.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
```

---

## Task 3: DTOs

- [ ] Crear `src/main/java/com/smartcommerce/cart/dto/request/AddCartItemRequest.java`:

```java
package com.smartcommerce.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCartItemRequest {

    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/cart/dto/request/UpdateCartItemRequest.java`:

```java
package com.smartcommerce.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCartItemRequest {

    @NotNull
    @Min(1)
    private Integer quantity;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/cart/dto/request/MergeCartRequest.java`:

```java
package com.smartcommerce.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MergeCartRequest {

    @NotNull
    private List<AddCartItemRequest> items;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/cart/dto/response/CartItemResponseDTO.java`:

```java
package com.smartcommerce.cart.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CartItemResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/cart/dto/response/CartResponseDTO.java`:

```java
package com.smartcommerce.cart.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CartResponseDTO {
    private Long id;
    private List<CartItemResponseDTO> items;
    private BigDecimal total;
}
```

---

## Task 4: CartMapper

- [ ] Crear `src/main/java/com/smartcommerce/cart/mapper/CartMapper.java`:

```java
package com.smartcommerce.cart.mapper;

import com.smartcommerce.cart.dto.response.CartItemResponseDTO;
import com.smartcommerce.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.price", target = "unitPrice")
    @Mapping(target = "subtotal", expression = "java(item.getProduct().getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    CartItemResponseDTO toItemDTO(CartItem item);
}
```

---

## Task 5: Excepciones

- [ ] Crear `src/main/java/com/smartcommerce/exception/InsufficientStockException.java`:

```java
package com.smartcommerce.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName, int available) {
        super("Stock insuficiente para '" + productName + "'. Disponible: " + available);
    }
}
```

- [ ] Crear `src/main/java/com/smartcommerce/exception/CartItemNotFoundException.java`:

```java
package com.smartcommerce.exception;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(Long id) {
        super("Item del carrito no encontrado con id: " + id);
    }
}
```

- [ ] Añadir handlers en `GlobalExceptionHandler.java`:

```java
@ExceptionHandler(InsufficientStockException.class)
public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .message(ex.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
}

@ExceptionHandler(CartItemNotFoundException.class)
public ResponseEntity<ErrorResponse> handleCartItemNotFound(CartItemNotFoundException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}
```

---

## Task 6: CartService

- [ ] Crear `src/main/java/com/smartcommerce/cart/service/CartService.java`:

```java
package com.smartcommerce.cart.service;

import com.smartcommerce.cart.dto.request.AddCartItemRequest;
import com.smartcommerce.cart.dto.request.MergeCartRequest;
import com.smartcommerce.cart.dto.request.UpdateCartItemRequest;
import com.smartcommerce.cart.dto.response.CartResponseDTO;
import com.smartcommerce.cart.entity.Cart;

public interface CartService {
    CartResponseDTO getCart(Long userId);
    CartResponseDTO addItem(Long userId, AddCartItemRequest request);
    CartResponseDTO updateItem(Long userId, Long itemId, UpdateCartItemRequest request);
    void removeItem(Long userId, Long itemId);
    void clearCart(Long userId);
    CartResponseDTO mergeCart(Long userId, MergeCartRequest request);
    Cart getOrCreateCart(Long userId);
}
```

---

## Task 7: CartServiceImpl

- [ ] **Escribir el test primero** `src/test/java/com/smartcommerce/cart/service/CartServiceTest.java`:

```java
package com.smartcommerce.cart.service;

import com.smartcommerce.cart.dto.request.AddCartItemRequest;
import com.smartcommerce.cart.dto.response.CartResponseDTO;
import com.smartcommerce.cart.entity.Cart;
import com.smartcommerce.cart.entity.CartItem;
import com.smartcommerce.cart.mapper.CartMapper;
import com.smartcommerce.cart.repository.CartItemRepository;
import com.smartcommerce.cart.repository.CartRepository;
import com.smartcommerce.cart.service.impl.CartServiceImpl;
import com.smartcommerce.exception.InsufficientStockException;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.entity.ProductStatus;
import com.smartcommerce.product.repository.ProductRepository;
import com.smartcommerce.user.entity.Role;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock CartMapper cartMapper;
    @InjectMocks CartServiceImpl cartService;

    private User mockUser(Long id) {
        return User.builder().id(id).email("u@test.com").role(Role.USER).active(true).build();
    }

    private Product mockProduct(Long id, int stock) {
        return Product.builder()
                .id(id).name("Producto Test").price(BigDecimal.TEN)
                .stock(stock).status(ProductStatus.ACTIVE).build();
    }

    private Cart mockCart(User user) {
        return Cart.builder().id(1L).user(user).items(new ArrayList<>()).build();
    }

    @Test
    void addItem_withInsufficientStock_throwsException() {
        User user = mockUser(1L);
        Product product = mockProduct(1L, 0);
        Cart cart = mockCart(user);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        AddCartItemRequest req = new AddCartItemRequest();
        req.setProductId(1L);
        req.setQuantity(1);

        assertThatThrownBy(() -> cartService.addItem(1L, req))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void addItem_newProduct_addsToCart() {
        User user = mockUser(1L);
        Product product = mockProduct(1L, 10);
        Cart cart = mockCart(user);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenReturn(cart);

        AddCartItemRequest req = new AddCartItemRequest();
        req.setProductId(1L);
        req.setQuantity(2);

        cartService.addItem(1L, req);

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItem_existingProduct_incrementsQuantity() {
        User user = mockUser(1L);
        Product product = mockProduct(1L, 10);
        Cart cart = mockCart(user);
        CartItem existingItem = CartItem.builder().id(1L).cart(cart).product(product).quantity(3).build();

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(existingItem));

        AddCartItemRequest req = new AddCartItemRequest();
        req.setProductId(1L);
        req.setQuantity(2);

        cartService.addItem(1L, req);

        verify(cartItemRepository).save(existingItem);
        assert existingItem.getQuantity() == 5;
    }
}
```

- [ ] Ejecutar el test para verificar que falla:

```bash
./mvnw test -Dtest=CartServiceTest
```

- [ ] Implementar `src/main/java/com/smartcommerce/cart/service/impl/CartServiceImpl.java`:

```java
package com.smartcommerce.cart.service.impl;

import com.smartcommerce.cart.dto.request.AddCartItemRequest;
import com.smartcommerce.cart.dto.request.MergeCartRequest;
import com.smartcommerce.cart.dto.request.UpdateCartItemRequest;
import com.smartcommerce.cart.dto.response.CartItemResponseDTO;
import com.smartcommerce.cart.dto.response.CartResponseDTO;
import com.smartcommerce.cart.entity.Cart;
import com.smartcommerce.cart.entity.CartItem;
import com.smartcommerce.cart.mapper.CartMapper;
import com.smartcommerce.cart.repository.CartItemRepository;
import com.smartcommerce.cart.repository.CartRepository;
import com.smartcommerce.cart.service.CartService;
import com.smartcommerce.exception.CartItemNotFoundException;
import com.smartcommerce.exception.InsufficientStockException;
import com.smartcommerce.exception.ProductNotFoundException;
import com.smartcommerce.exception.UserNotFoundException;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.entity.ProductStatus;
import com.smartcommerce.product.repository.ProductRepository;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Override
    public CartResponseDTO getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponseDTO addItem(Long userId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        validateStock(product, request.getQuantity());

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQty = item.getQuantity() + request.getQuantity();
            validateStock(product, newQty);
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart).product(product).quantity(request.getQuantity()).build();
            cart.getItems().add(item);
            cartRepository.save(cart);
        }

        return buildCartResponse(cartRepository.findByUserId(userId).orElseThrow());
    }

    @Override
    @Transactional
    public CartResponseDTO updateItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(itemId));

        validateStock(item.getProduct(), request.getQuantity());
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(itemId));
        cart.getItems().remove(item);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public CartResponseDTO mergeCart(Long userId, MergeCartRequest request) {
        for (AddCartItemRequest item : request.getItems()) {
            try {
                addItem(userId, item);
            } catch (InsufficientStockException | ProductNotFoundException ignored) {
                // Se ignoran items inválidos durante la fusión
            }
        }
        return getCart(userId);
    }

    @Override
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
            Cart newCart = Cart.builder().user(user).build();
            return cartRepository.save(newCart);
        });
    }

    private void validateStock(Product product, int quantity) {
        if (product.getStatus() == ProductStatus.DISABLED
                || product.getStatus() == ProductStatus.OUT_OF_STOCK
                || product.getStock() < quantity) {
            throw new InsufficientStockException(product.getName(), product.getStock());
        }
    }

    private CartResponseDTO buildCartResponse(Cart cart) {
        List<CartItemResponseDTO> items = cart.getItems().stream()
                .map(cartMapper::toItemDTO)
                .toList();
        BigDecimal total = items.stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartResponseDTO.builder().id(cart.getId()).items(items).total(total).build();
    }
}
```

- [ ] Ejecutar el test:

```bash
./mvnw test -Dtest=CartServiceTest
```

Resultado esperado: `Tests run: 3, Failures: 0, Errors: 0`

---

## Task 8: CartController

- [ ] Crear `src/main/java/com/smartcommerce/cart/controller/CartController.java`:

```java
package com.smartcommerce.cart.controller;

import com.smartcommerce.cart.dto.request.AddCartItemRequest;
import com.smartcommerce.cart.dto.request.MergeCartRequest;
import com.smartcommerce.cart.dto.request.UpdateCartItemRequest;
import com.smartcommerce.cart.dto.response.CartResponseDTO;
import com.smartcommerce.cart.service.CartService;
import com.smartcommerce.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.getCart(user.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.addItem(user.getId(), request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDTO> updateItem(
            Authentication authentication,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.updateItem(user.getId(), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            Authentication authentication,
            @PathVariable Long itemId) {
        User user = (User) authentication.getPrincipal();
        cartService.removeItem(user.getId(), itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        cartService.clearCart(user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merge")
    public ResponseEntity<CartResponseDTO> mergeCart(
            Authentication authentication,
            @Valid @RequestBody MergeCartRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.mergeCart(user.getId(), request));
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
git add src/main/java/com/smartcommerce/cart/ \
        src/main/java/com/smartcommerce/exception/ \
        src/test/java/com/smartcommerce/cart/
git commit -m "feat(cart): add persistent cart with stock validation and merge endpoint"
```
