# Phase 12 — Product Filter Improvements

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans`.

**Goal:** Ampliar el sistema de filtrado de productos ya existente añadiendo opciones de ordenación: precio ascendente/descendente, más recientes, mejor valorados (media de estrellas) y más vendidos (unidades vendidas en pedidos DELIVERED).

**Architecture:** Extender `ProductFilterDTO` con un campo `sortBy`. Añadir queries en `ProductRepository`. Actualizar `ProductServiceImpl.getAllProducts()` para construir el `Pageable` con el sort correcto según el campo solicitado.

**Pre-requisitos:** Phase 01 completada (módulo product ya existe). Phase 08 y 11 completadas (OrderItem y Review existen en BD para las consultas de sorting).

---

## Archivos a modificar

| Acción | Archivo |
|--------|---------|
| Modificar | `src/main/java/com/smartcommerce/product/dto/request/ProductFilterDTO.java` |
| Modificar | `src/main/java/com/smartcommerce/product/repository/ProductRepository.java` |
| Modificar | `src/main/java/com/smartcommerce/product/service/impl/ProductServiceImpl.java` |
| Crear | `src/test/java/com/smartcommerce/product/service/ProductSortingTest.java` |

---

## Task 1: Añadir enum SortBy y actualizar ProductFilterDTO

- [ ] Crear `src/main/java/com/smartcommerce/product/dto/request/ProductSortBy.java`:

```java
package com.smartcommerce.product.dto.request;

public enum ProductSortBy {
    PRICE_ASC,
    PRICE_DESC,
    NEWEST,
    BEST_RATED,
    BEST_SELLERS
}
```

- [ ] Abrir `src/main/java/com/smartcommerce/product/dto/request/ProductFilterDTO.java` y añadir el campo `sortBy`:

```java
// Añadir este import al inicio del archivo:
// (ya debería tener los imports de BigDecimal etc.)

// Añadir este campo dentro de la clase ProductFilterDTO:
private ProductSortBy sortBy;
```

El archivo completo resultante debe verse así (verificar que los campos existentes se mantienen):

```java
package com.smartcommerce.product.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductFilterDTO {
    private String name;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String brand;
    private Boolean available;
    private ProductSortBy sortBy;
}
```

> **Nota:** Si `categoryId` o `available` no existen en el ProductFilterDTO actual, añadirlos también.

---

## Task 2: Añadir queries en ProductRepository

- [ ] Abrir `src/main/java/com/smartcommerce/product/repository/ProductRepository.java` y añadir:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Añadir estos métodos al interface:

@Query("SELECT p FROM Product p LEFT JOIN Review r ON r.product.id = p.id " +
       "GROUP BY p.id ORDER BY AVG(r.rating) DESC NULLS LAST")
Page<com.smartcommerce.product.entity.Product> findAllOrderByBestRated(Pageable pageable);

@Query("SELECT p FROM Product p LEFT JOIN OrderItem oi ON oi.product.id = p.id " +
       "LEFT JOIN Order o ON o.id = oi.order.id AND o.status = 'DELIVERED' " +
       "GROUP BY p.id ORDER BY COALESCE(SUM(oi.quantity), 0) DESC")
Page<com.smartcommerce.product.entity.Product> findAllOrderByBestSellers(Pageable pageable);
```

> **Nota:** Estos métodos se usan solo para `BEST_RATED` y `BEST_SELLERS`. Para `PRICE_ASC`, `PRICE_DESC` y `NEWEST` se usa Spring Data Pageable con `Sort` estándar.

---

## Task 3: Actualizar ProductServiceImpl

- [ ] Abrir `src/main/java/com/smartcommerce/product/service/impl/ProductServiceImpl.java`.

- [ ] Añadir el import al inicio:

```java
import com.smartcommerce.product.dto.request.ProductSortBy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
```

- [ ] Reemplazar el método `getAllProducts` completo con esta versión que incluye el filtro de `categoryId`, `available` y el `sortBy`:

```java
@Override
public Page<ProductResponseDTO> getAllProducts(ProductFilterDTO filter, Pageable pageable) {

    // Sorting especial para best_rated y best_sellers (requieren queries propias)
    if (filter.getSortBy() == ProductSortBy.BEST_RATED) {
        Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAllOrderByBestRated(unsorted).map(productMapper::toDTO);
    }
    if (filter.getSortBy() == ProductSortBy.BEST_SELLERS) {
        Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAllOrderByBestSellers(unsorted).map(productMapper::toDTO);
    }

    // Sorting estándar via Pageable
    Sort sort = resolveSort(filter.getSortBy());
    Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

    Specification<Product> spec = (root, query, cb) -> cb.conjunction();

    if (filter.getMinPrice() != null) {
        spec = spec.and(ProductSpecification.hasMinPrice(filter.getMinPrice()));
    }
    if (filter.getMaxPrice() != null) {
        spec = spec.and(ProductSpecification.hasMaxPrice(filter.getMaxPrice()));
    }
    if (filter.getBrand() != null && !filter.getBrand().isBlank()) {
        spec = spec.and(ProductSpecification.hasBrand(filter.getBrand()));
    }
    if (filter.getName() != null && !filter.getName().isBlank()) {
        spec = spec.and(ProductSpecification.nameContains(filter.getName()));
    }
    if (filter.getCategoryId() != null) {
        spec = spec.and(ProductSpecification.hasCategory(filter.getCategoryId()));
    }
    if (Boolean.TRUE.equals(filter.getAvailable())) {
        spec = spec.and(ProductSpecification.isAvailable());
    }

    return productRepository.findAll(spec, sortedPageable).map(productMapper::toDTO);
}

private Sort resolveSort(ProductSortBy sortBy) {
    if (sortBy == null) return Sort.by(Sort.Direction.DESC, "createdAt");
    return switch (sortBy) {
        case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
        case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price");
        case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");
        default -> Sort.by(Sort.Direction.DESC, "createdAt");
    };
}
```

---

## Task 4: Añadir specs que falten en ProductSpecification

- [ ] Abrir `src/main/java/com/smartcommerce/product/repository/ProductSpecification.java` y verificar que existen `hasCategory` e `isAvailable`. Si no existen, añadir:

```java
public static Specification<Product> hasCategory(Long categoryId) {
    return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
}

public static Specification<Product> isAvailable() {
    return (root, query, cb) -> cb.greaterThan(root.get("stock"), 0);
}
```

---

## Task 5: Tests de sorting

- [ ] **Escribir el test** `src/test/java/com/smartcommerce/product/service/ProductSortingTest.java`:

```java
package com.smartcommerce.product.service;

import com.smartcommerce.product.dto.request.ProductFilterDTO;
import com.smartcommerce.product.dto.request.ProductSortBy;
import com.smartcommerce.product.dto.response.ProductResponseDTO;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.entity.ProductStatus;
import com.smartcommerce.product.mapper.ProductMapper;
import com.smartcommerce.product.repository.ProductRepository;
import com.smartcommerce.product.repository.ProductSpecification;
import com.smartcommerce.product.service.impl.ProductServiceImpl;
import com.smartcommerce.category.repository.CategoryRepository;
import com.smartcommerce.productimage.mapper.ProductImageMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSortingTest {

    @Mock ProductRepository productRepository;
    @Mock ProductMapper productMapper;
    @Mock CategoryRepository categoryRepository;
    @Mock ProductImageMapper productImageMapper;
    @InjectMocks ProductServiceImpl productService;

    private Product mockProduct(Long id, BigDecimal price) {
        return Product.builder().id(id).name("P" + id).price(price)
                .stock(5).status(ProductStatus.ACTIVE).build();
    }

    @Test
    void getAllProducts_withPriceAscSort_callsRepositoryWithPriceAscSort() {
        ProductFilterDTO filter = new ProductFilterDTO();
        filter.setSortBy(ProductSortBy.PRICE_ASC);
        Pageable pageable = PageRequest.of(0, 10);

        Product p1 = mockProduct(1L, BigDecimal.valueOf(10));
        ProductResponseDTO dto = ProductResponseDTO.builder().build();
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p1)));
        when(productMapper.toDTO(any())).thenReturn(dto);

        Page<ProductResponseDTO> result = productService.getAllProducts(filter, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findAll(any(Specification.class),
                argThat((Pageable p) -> p.getSort().getOrderFor("price") != null
                        && p.getSort().getOrderFor("price").getDirection() == Sort.Direction.ASC));
    }

    @Test
    void getAllProducts_withBestRatedSort_callsFindAllOrderByBestRated() {
        ProductFilterDTO filter = new ProductFilterDTO();
        filter.setSortBy(ProductSortBy.BEST_RATED);
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findAllOrderByBestRated(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        productService.getAllProducts(filter, pageable);

        verify(productRepository).findAllOrderByBestRated(any(Pageable.class));
    }

    private <T> T argThat(org.mockito.ArgumentMatcher<T> matcher) {
        return org.mockito.ArgumentMatchers.argThat(matcher);
    }
}
```

- [ ] Ejecutar el test para verificar que falla:

```bash
./mvnw test -Dtest=ProductSortingTest
```

- [ ] Ejecutar el test tras los cambios:

```bash
./mvnw test -Dtest=ProductSortingTest
```

Resultado esperado: `Tests run: 2, Failures: 0, Errors: 0`

---

## Task 6: Verificar todos los tests y commit

- [ ] Ejecutar todos los tests:

```bash
./mvnw test
```

Resultado esperado: `BUILD SUCCESS`

- [ ] Commit:

```bash
git add src/main/java/com/smartcommerce/product/ \
        src/test/java/com/smartcommerce/product/
git commit -m "feat(product): add sortBy filter (PRICE_ASC, PRICE_DESC, NEWEST, BEST_RATED, BEST_SELLERS)"
```
