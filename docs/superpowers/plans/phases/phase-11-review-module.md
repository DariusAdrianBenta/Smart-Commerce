# Phase 11 — Review Module

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans`.

**Goal:** Los usuarios autenticados que tengan el producto en un pedido con estado `DELIVERED` pueden dejar una reseña (texto + estrellas 1-5). Un usuario solo puede dejar una reseña por producto.

**Architecture:** `ReviewController` → `ReviewServiceImpl` → `ReviewRepository` + `OrderRepository`. La validación de compra verifica que el usuario tenga un `OrderItem` con ese `productId` en un pedido `DELIVERED`.

**Pre-requisitos:** Phase 08 completada (`Order`, `OrderItem`, `OrderRepository` existen).

---

## Archivos a crear

| Acción | Archivo |
|--------|---------|
| Crear | `src/main/java/com/smartcommerce/review/entity/Review.java` |
| Crear | `src/main/java/com/smartcommerce/review/repository/ReviewRepository.java` |
| Crear | `src/main/java/com/smartcommerce/review/dto/request/ReviewRequest.java` |
| Crear | `src/main/java/com/smartcommerce/review/dto/response/ReviewResponseDTO.java` |
| Crear | `src/main/java/com/smartcommerce/review/mapper/ReviewMapper.java` |
| Crear | `src/main/java/com/smartcommerce/review/service/ReviewService.java` |
| Crear | `src/main/java/com/smartcommerce/review/service/impl/ReviewServiceImpl.java` |
| Crear | `src/main/java/com/smartcommerce/review/controller/ReviewController.java` |
| Modificar | `src/main/java/com/smartcommerce/exception/GlobalExceptionHandler.java` |
| Modificar | `src/main/java/com/smartcommerce/order/repository/OrderRepository.java` |
| Crear | `src/test/java/com/smartcommerce/review/service/ReviewServiceTest.java` |

---

## Task 1: Entidad Review

- [ ] Crear `src/main/java/com/smartcommerce/review/entity/Review.java`:

```java
package com.smartcommerce.review.entity;

import com.smartcommerce.product.entity.Product;
import com.smartcommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 1000)
    private String comment;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
```

---

## Task 2: ReviewRepository

- [ ] Crear `src/main/java/com/smartcommerce/review/repository/ReviewRepository.java`:

```java
package com.smartcommerce.review.repository;

import com.smartcommerce.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductId(Long productId, Pageable pageable);
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
```

---

## Task 3: Añadir query a OrderRepository

- [ ] Abrir `src/main/java/com/smartcommerce/order/repository/OrderRepository.java` y añadir:

```java
import com.smartcommerce.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.Query;

// Añadir este método al interface:
@Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
       "WHERE oi.order.user.id = :userId " +
       "AND oi.product.id = :productId " +
       "AND oi.order.status = :status")
boolean existsDeliveredOrderWithProduct(
    @org.springframework.data.repository.query.Param("userId") Long userId,
    @org.springframework.data.repository.query.Param("productId") Long productId,
    @org.springframework.data.repository.query.Param("status") OrderStatus status);
```

---

## Task 4: DTOs

- [ ] Crear `src/main/java/com/smartcommerce/review/dto/request/ReviewRequest.java`:

```java
package com.smartcommerce.review.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotBlank
    @Size(min = 10, max = 1000, message = "El comentario debe tener entre 10 y 1000 caracteres")
    private String comment;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/review/dto/response/ReviewResponseDTO.java`:

```java
package com.smartcommerce.review.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponseDTO {
    private Long id;
    private Long userId;
    private String userFirstName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
```

---

## Task 5: ReviewMapper

- [ ] Crear `src/main/java/com/smartcommerce/review/mapper/ReviewMapper.java`:

```java
package com.smartcommerce.review.mapper;

import com.smartcommerce.review.dto.response.ReviewResponseDTO;
import com.smartcommerce.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.firstName", target = "userFirstName")
    ReviewResponseDTO toDTO(Review review);
}
```

---

## Task 6: Excepciones

- [ ] Crear `src/main/java/com/smartcommerce/exception/ReviewAlreadyExistsException.java`:

```java
package com.smartcommerce.exception;

public class ReviewAlreadyExistsException extends RuntimeException {
    public ReviewAlreadyExistsException(Long productId) {
        super("Ya has dejado una reseña para el producto con id: " + productId);
    }
}
```

- [ ] Crear `src/main/java/com/smartcommerce/exception/ReviewNotAllowedException.java`:

```java
package com.smartcommerce.exception;

public class ReviewNotAllowedException extends RuntimeException {
    public ReviewNotAllowedException() {
        super("Solo puedes reseñar productos que hayas comprado y recibido");
    }
}
```

- [ ] Añadir handlers en `GlobalExceptionHandler.java`:

```java
@ExceptionHandler(ReviewAlreadyExistsException.class)
public ResponseEntity<ErrorResponse> handleReviewAlreadyExists(ReviewAlreadyExistsException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .message(ex.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
}

@ExceptionHandler(ReviewNotAllowedException.class)
public ResponseEntity<ErrorResponse> handleReviewNotAllowed(ReviewNotAllowedException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .message(ex.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
}
```

---

## Task 7: ReviewService

- [ ] Crear `src/main/java/com/smartcommerce/review/service/ReviewService.java`:

```java
package com.smartcommerce.review.service;

import com.smartcommerce.review.dto.request.ReviewRequest;
import com.smartcommerce.review.dto.response.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    Page<ReviewResponseDTO> getProductReviews(Long productId, Pageable pageable);
    ReviewResponseDTO createReview(Long userId, Long productId, ReviewRequest request);
    ReviewResponseDTO updateReview(Long userId, Long reviewId, ReviewRequest request);
    void deleteReview(Long userId, Long reviewId);
}
```

---

## Task 8: ReviewServiceImpl

- [ ] **Escribir el test primero** `src/test/java/com/smartcommerce/review/service/ReviewServiceTest.java`:

```java
package com.smartcommerce.review.service;

import com.smartcommerce.exception.ReviewAlreadyExistsException;
import com.smartcommerce.exception.ReviewNotAllowedException;
import com.smartcommerce.order.entity.OrderStatus;
import com.smartcommerce.order.repository.OrderRepository;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.entity.ProductStatus;
import com.smartcommerce.product.repository.ProductRepository;
import com.smartcommerce.review.dto.request.ReviewRequest;
import com.smartcommerce.review.mapper.ReviewMapper;
import com.smartcommerce.review.repository.ReviewRepository;
import com.smartcommerce.review.service.impl.ReviewServiceImpl;
import com.smartcommerce.user.entity.Role;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock ReviewRepository reviewRepository;
    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock ReviewMapper reviewMapper;
    @InjectMocks ReviewServiceImpl reviewService;

    private User mockUser(Long id) {
        return User.builder().id(id).email("u@test.com").firstName("Juan")
                .role(Role.USER).active(true).build();
    }

    private Product mockProduct(Long id) {
        return Product.builder().id(id).name("Prod").price(BigDecimal.TEN)
                .stock(5).status(ProductStatus.ACTIVE).build();
    }

    @Test
    void createReview_userHasNotPurchasedProduct_throwsException() {
        when(orderRepository.existsDeliveredOrderWithProduct(1L, 10L, OrderStatus.DELIVERED))
                .thenReturn(false);

        ReviewRequest req = new ReviewRequest();
        req.setRating(5);
        req.setComment("Excelente producto, muy recomendable.");

        assertThatThrownBy(() -> reviewService.createReview(1L, 10L, req))
                .isInstanceOf(ReviewNotAllowedException.class);
    }

    @Test
    void createReview_userAlreadyReviewed_throwsException() {
        when(orderRepository.existsDeliveredOrderWithProduct(1L, 10L, OrderStatus.DELIVERED))
                .thenReturn(true);
        when(reviewRepository.existsByUserIdAndProductId(1L, 10L)).thenReturn(true);

        ReviewRequest req = new ReviewRequest();
        req.setRating(4);
        req.setComment("Muy buen producto, lo recomiendo.");

        assertThatThrownBy(() -> reviewService.createReview(1L, 10L, req))
                .isInstanceOf(ReviewAlreadyExistsException.class);
    }

    @Test
    void createReview_validUser_savesReview() {
        User user = mockUser(1L);
        Product product = mockProduct(10L);

        when(orderRepository.existsDeliveredOrderWithProduct(1L, 10L, OrderStatus.DELIVERED))
                .thenReturn(true);
        when(reviewRepository.existsByUserIdAndProductId(1L, 10L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(reviewRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(inv -> inv.getArgument(0));

        ReviewRequest req = new ReviewRequest();
        req.setRating(5);
        req.setComment("Producto increíble, superó mis expectativas totalmente.");

        reviewService.createReview(1L, 10L, req);

        org.mockito.Mockito.verify(reviewRepository).save(org.mockito.ArgumentMatchers.any());
    }
}
```

- [ ] Ejecutar el test para verificar que falla:

```bash
./mvnw test -Dtest=ReviewServiceTest
```

- [ ] Implementar `src/main/java/com/smartcommerce/review/service/impl/ReviewServiceImpl.java`:

```java
package com.smartcommerce.review.service.impl;

import com.smartcommerce.exception.ProductNotFoundException;
import com.smartcommerce.exception.ReviewAlreadyExistsException;
import com.smartcommerce.exception.ReviewNotAllowedException;
import com.smartcommerce.exception.UserNotFoundException;
import com.smartcommerce.order.entity.OrderStatus;
import com.smartcommerce.order.repository.OrderRepository;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.repository.ProductRepository;
import com.smartcommerce.review.dto.request.ReviewRequest;
import com.smartcommerce.review.dto.response.ReviewResponseDTO;
import com.smartcommerce.review.entity.Review;
import com.smartcommerce.review.mapper.ReviewMapper;
import com.smartcommerce.review.repository.ReviewRepository;
import com.smartcommerce.review.service.ReviewService;
import com.smartcommerce.user.entity.User;
import com.smartcommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    public Page<ReviewResponseDTO> getProductReviews(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable).map(reviewMapper::toDTO);
    }

    @Override
    public ReviewResponseDTO createReview(Long userId, Long productId, ReviewRequest request) {
        if (!orderRepository.existsDeliveredOrderWithProduct(userId, productId, OrderStatus.DELIVERED)) {
            throw new ReviewNotAllowedException();
        }
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new ReviewAlreadyExistsException(productId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return reviewMapper.toDTO(reviewRepository.save(review));
    }

    @Override
    public ReviewResponseDTO updateReview(Long userId, Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));
        if (!review.getUser().getId().equals(userId)) {
            throw new ReviewNotAllowedException();
        }
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return reviewMapper.toDTO(reviewRepository.save(review));
    }

    @Override
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));
        if (!review.getUser().getId().equals(userId)) {
            throw new ReviewNotAllowedException();
        }
        reviewRepository.delete(review);
    }
}
```

- [ ] Ejecutar el test:

```bash
./mvnw test -Dtest=ReviewServiceTest
```

Resultado esperado: `Tests run: 3, Failures: 0, Errors: 0`

---

## Task 9: ReviewController

- [ ] Crear `src/main/java/com/smartcommerce/review/controller/ReviewController.java`:

```java
package com.smartcommerce.review.controller;

import com.smartcommerce.review.dto.request.ReviewRequest;
import com.smartcommerce.review.dto.response.ReviewResponseDTO;
import com.smartcommerce.review.service.ReviewService;
import com.smartcommerce.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponseDTO>> getProductReviews(
            @PathVariable Long productId, Pageable pageable) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, pageable));
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ReviewResponseDTO> createReview(
            Authentication authentication,
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(user.getId(), productId, request));
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            Authentication authentication,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(reviewService.updateReview(user.getId(), reviewId, request));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            Authentication authentication,
            @PathVariable Long reviewId) {
        User user = (User) authentication.getPrincipal();
        reviewService.deleteReview(user.getId(), reviewId);
        return ResponseEntity.noContent().build();
    }
}
```

> El endpoint `GET /api/v1/products/{productId}/reviews` es público — ya está configurado en `SecurityConfig` de Phase 01: `requestMatchers(HttpMethod.GET, "/api/v1/products/*/reviews").permitAll()`

---

## Task 10: Verificar y commit

- [ ] Ejecutar todos los tests:

```bash
./mvnw test
```

Resultado esperado: `BUILD SUCCESS`

- [ ] Commit:

```bash
git add src/main/java/com/smartcommerce/review/ \
        src/main/java/com/smartcommerce/exception/ \
        src/main/java/com/smartcommerce/order/repository/ \
        src/test/java/com/smartcommerce/review/
git commit -m "feat(review): add product reviews restricted to verified buyers with DELIVERED orders"
```
