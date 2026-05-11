# Phase 06 — Coupon Module

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans`.

**Goal:** El ADMIN puede crear cupones de descuento (código único, tipo PERCENTAGE o FIXED_AMOUNT, fecha de expiración, límite de usos opcional). El usuario puede validar un cupón antes del checkout. La aplicación del cupón al pedido ocurre en Phase 08 (Order).

**Architecture:** `CouponController` → `CouponServiceImpl` → `CouponRepository`. ADMIN gestiona via `/api/v1/admin/coupons/**`. Usuario valida via `GET /api/v1/coupons/validate/{code}`.

**Pre-requisitos:** Phase 01 y 02 completadas.

---

## Archivos a crear

| Acción | Archivo |
|--------|---------|
| Crear | `src/main/java/com/smartcommerce/coupon/entity/DiscountType.java` |
| Crear | `src/main/java/com/smartcommerce/coupon/entity/Coupon.java` |
| Crear | `src/main/java/com/smartcommerce/coupon/repository/CouponRepository.java` |
| Crear | `src/main/java/com/smartcommerce/coupon/dto/request/CouponRequest.java` |
| Crear | `src/main/java/com/smartcommerce/coupon/dto/response/CouponResponseDTO.java` |
| Crear | `src/main/java/com/smartcommerce/coupon/mapper/CouponMapper.java` |
| Crear | `src/main/java/com/smartcommerce/coupon/service/CouponService.java` |
| Crear | `src/main/java/com/smartcommerce/coupon/service/impl/CouponServiceImpl.java` |
| Crear | `src/main/java/com/smartcommerce/coupon/controller/CouponController.java` |
| Modificar | `src/main/java/com/smartcommerce/exception/GlobalExceptionHandler.java` |
| Crear | `src/test/java/com/smartcommerce/coupon/service/CouponServiceTest.java` |

---

## Task 1: Enum y entidad

- [ ] Crear `src/main/java/com/smartcommerce/coupon/entity/DiscountType.java`:

```java
package com.smartcommerce.coupon.entity;

public enum DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT
}
```

- [ ] Crear `src/main/java/com/smartcommerce/coupon/entity/Coupon.java`:

```java
package com.smartcommerce.coupon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean active;

    private Integer usageLimit;

    @Column(nullable = false)
    @Builder.Default
    private Integer usageCount = 0;
}
```

---

## Task 2: CouponRepository

- [ ] Crear `src/main/java/com/smartcommerce/coupon/repository/CouponRepository.java`:

```java
package com.smartcommerce.coupon.repository;

import com.smartcommerce.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
    boolean existsByCode(String code);
}
```

---

## Task 3: DTOs

- [ ] Crear `src/main/java/com/smartcommerce/coupon/dto/request/CouponRequest.java`:

```java
package com.smartcommerce.coupon.dto.request;

import com.smartcommerce.coupon.entity.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CouponRequest {

    @NotBlank
    private String code;

    @NotNull
    private DiscountType discountType;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal discountValue;

    @NotNull
    @Future
    private LocalDateTime expiresAt;

    private Integer usageLimit;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/coupon/dto/response/CouponResponseDTO.java`:

```java
package com.smartcommerce.coupon.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CouponResponseDTO {
    private Long id;
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private LocalDateTime expiresAt;
    private boolean active;
    private Integer usageLimit;
    private Integer usageCount;
}
```

---

## Task 4: CouponMapper

- [ ] Crear `src/main/java/com/smartcommerce/coupon/mapper/CouponMapper.java`:

```java
package com.smartcommerce.coupon.mapper;

import com.smartcommerce.coupon.dto.response.CouponResponseDTO;
import com.smartcommerce.coupon.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CouponMapper {

    @Mapping(source = "discountType", target = "discountType", qualifiedByName = "typeToString")
    CouponResponseDTO toDTO(Coupon coupon);

    @org.mapstruct.Named("typeToString")
    default String typeToString(com.smartcommerce.coupon.entity.DiscountType type) {
        return type != null ? type.name() : null;
    }
}
```

---

## Task 5: Excepciones

- [ ] Crear `src/main/java/com/smartcommerce/exception/CouponNotFoundException.java`:

```java
package com.smartcommerce.exception;

public class CouponNotFoundException extends RuntimeException {
    public CouponNotFoundException(String code) {
        super("Cupón no encontrado: " + code);
    }
}
```

- [ ] Crear `src/main/java/com/smartcommerce/exception/CouponExpiredException.java`:

```java
package com.smartcommerce.exception;

public class CouponExpiredException extends RuntimeException {
    public CouponExpiredException(String code) {
        super("El cupón '" + code + "' está expirado o inactivo");
    }
}
```

- [ ] Añadir handlers en `GlobalExceptionHandler.java`:

```java
@ExceptionHandler(CouponNotFoundException.class)
public ResponseEntity<ErrorResponse> handleCouponNotFound(CouponNotFoundException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}

@ExceptionHandler(CouponExpiredException.class)
public ResponseEntity<ErrorResponse> handleCouponExpired(CouponExpiredException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .message(ex.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
```

---

## Task 6: CouponService

- [ ] Crear `src/main/java/com/smartcommerce/coupon/service/CouponService.java`:

```java
package com.smartcommerce.coupon.service;

import com.smartcommerce.coupon.dto.request.CouponRequest;
import com.smartcommerce.coupon.dto.response.CouponResponseDTO;
import com.smartcommerce.coupon.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponService {
    CouponResponseDTO createCoupon(CouponRequest request);
    CouponResponseDTO updateCoupon(Long id, CouponRequest request);
    void deleteCoupon(Long id);
    Page<CouponResponseDTO> getAllCoupons(Pageable pageable);
    CouponResponseDTO validateCoupon(String code);
    Coupon getValidCouponByCode(String code);
    void incrementUsage(Coupon coupon);
}
```

---

## Task 7: CouponServiceImpl

- [ ] **Escribir el test primero** `src/test/java/com/smartcommerce/coupon/service/CouponServiceTest.java`:

```java
package com.smartcommerce.coupon.service;

import com.smartcommerce.coupon.entity.Coupon;
import com.smartcommerce.coupon.entity.DiscountType;
import com.smartcommerce.coupon.mapper.CouponMapper;
import com.smartcommerce.coupon.repository.CouponRepository;
import com.smartcommerce.coupon.service.impl.CouponServiceImpl;
import com.smartcommerce.exception.CouponExpiredException;
import com.smartcommerce.exception.CouponNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock CouponRepository couponRepository;
    @Mock CouponMapper couponMapper;
    @InjectMocks CouponServiceImpl couponService;

    private Coupon validCoupon() {
        return Coupon.builder()
                .id(1L).code("SAVE10").discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.TEN).expiresAt(LocalDateTime.now().plusDays(30))
                .active(true).usageCount(0).build();
    }

    @Test
    void validateCoupon_expiredCoupon_throwsException() {
        Coupon expired = Coupon.builder()
                .id(1L).code("OLD10").active(true)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .discountValue(BigDecimal.TEN).discountType(DiscountType.PERCENTAGE)
                .usageCount(0).build();
        when(couponRepository.findByCode("OLD10")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> couponService.validateCoupon("OLD10"))
                .isInstanceOf(CouponExpiredException.class);
    }

    @Test
    void validateCoupon_inactiveCoupon_throwsException() {
        Coupon inactive = validCoupon();
        inactive.setActive(false);
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> couponService.validateCoupon("SAVE10"))
                .isInstanceOf(CouponExpiredException.class);
    }

    @Test
    void validateCoupon_notFound_throwsException() {
        when(couponRepository.findByCode("NOEXIST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.validateCoupon("NOEXIST"))
                .isInstanceOf(CouponNotFoundException.class);
    }

    @Test
    void incrementUsage_atLimit_deactivatesCoupon() {
        Coupon coupon = validCoupon();
        coupon.setUsageLimit(1);
        coupon.setUsageCount(0);

        couponService.incrementUsage(coupon);

        assert coupon.getUsageCount() == 1;
        assert !coupon.isActive();
        verify(couponRepository).save(coupon);
    }
}
```

- [ ] Ejecutar test para verificar que falla:

```bash
./mvnw test -Dtest=CouponServiceTest
```

- [ ] Implementar `src/main/java/com/smartcommerce/coupon/service/impl/CouponServiceImpl.java`:

```java
package com.smartcommerce.coupon.service.impl;

import com.smartcommerce.coupon.dto.request.CouponRequest;
import com.smartcommerce.coupon.dto.response.CouponResponseDTO;
import com.smartcommerce.coupon.entity.Coupon;
import com.smartcommerce.coupon.mapper.CouponMapper;
import com.smartcommerce.coupon.repository.CouponRepository;
import com.smartcommerce.coupon.service.CouponService;
import com.smartcommerce.exception.CouponExpiredException;
import com.smartcommerce.exception.CouponNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    @Override
    public CouponResponseDTO createCoupon(CouponRequest request) {
        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .expiresAt(request.getExpiresAt())
                .usageLimit(request.getUsageLimit())
                .usageCount(0)
                .active(true)
                .build();
        return couponMapper.toDTO(couponRepository.save(coupon));
    }

    @Override
    public CouponResponseDTO updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(String.valueOf(id)));
        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setExpiresAt(request.getExpiresAt());
        coupon.setUsageLimit(request.getUsageLimit());
        return couponMapper.toDTO(couponRepository.save(coupon));
    }

    @Override
    public void deleteCoupon(Long id) {
        couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(String.valueOf(id)));
        couponRepository.deleteById(id);
    }

    @Override
    public Page<CouponResponseDTO> getAllCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable).map(couponMapper::toDTO);
    }

    @Override
    public CouponResponseDTO validateCoupon(String code) {
        return couponMapper.toDTO(getValidCouponByCode(code));
    }

    @Override
    public Coupon getValidCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new CouponNotFoundException(code));
        if (!coupon.isActive() || coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CouponExpiredException(code);
        }
        return coupon;
    }

    @Override
    public void incrementUsage(Coupon coupon) {
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            coupon.setActive(false);
        }
        couponRepository.save(coupon);
    }
}
```

- [ ] Ejecutar el test:

```bash
./mvnw test -Dtest=CouponServiceTest
```

Resultado esperado: `Tests run: 4, Failures: 0, Errors: 0`

---

## Task 8: CouponController

- [ ] Crear `src/main/java/com/smartcommerce/coupon/controller/CouponController.java`:

```java
package com.smartcommerce.coupon.controller;

import com.smartcommerce.coupon.dto.request.CouponRequest;
import com.smartcommerce.coupon.dto.response.CouponResponseDTO;
import com.smartcommerce.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/admin/coupons")
    public ResponseEntity<Page<CouponResponseDTO>> getAllCoupons(Pageable pageable) {
        return ResponseEntity.ok(couponService.getAllCoupons(pageable));
    }

    @PostMapping("/admin/coupons")
    public ResponseEntity<CouponResponseDTO> createCoupon(@Valid @RequestBody CouponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.createCoupon(request));
    }

    @PutMapping("/admin/coupons/{id}")
    public ResponseEntity<CouponResponseDTO> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.updateCoupon(id, request));
    }

    @DeleteMapping("/admin/coupons/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/coupons/validate/{code}")
    public ResponseEntity<CouponResponseDTO> validateCoupon(@PathVariable String code) {
        return ResponseEntity.ok(couponService.validateCoupon(code));
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
git add src/main/java/com/smartcommerce/coupon/ \
        src/main/java/com/smartcommerce/exception/ \
        src/test/java/com/smartcommerce/coupon/
git commit -m "feat(coupon): add coupon management with PERCENTAGE and FIXED_AMOUNT discount types"
```
