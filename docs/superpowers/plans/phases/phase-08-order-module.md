# Phase 08 — Order Module

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans`.

**Goal:** Implementar el flujo de checkout y gestión de pedidos. Al crear un pedido: valida el carrito, snapshot de dirección y productos, descuenta stock, procesa el pago con Stripe, vacía el carrito. El ADMIN puede ver todos los pedidos y cambiar estados manualmente.

**Architecture:** `OrderController` → `OrderServiceImpl` → `CartService` + `PaymentService` + `CouponService` + `AddressRepository` + `ProductRepository` + `OrderRepository`.

**Pre-requisitos:** Phases 04, 05, 06, 07 completadas.

---

## Archivos a crear

| Acción | Archivo |
|--------|---------|
| Crear | `src/main/java/com/smartcommerce/order/entity/OrderStatus.java` |
| Crear | `src/main/java/com/smartcommerce/order/entity/Order.java` |
| Crear | `src/main/java/com/smartcommerce/order/entity/OrderItem.java` |
| Crear | `src/main/java/com/smartcommerce/order/entity/OrderStatusHistory.java` |
| Crear | `src/main/java/com/smartcommerce/order/repository/OrderRepository.java` |
| Crear | `src/main/java/com/smartcommerce/order/repository/OrderStatusHistoryRepository.java` |
| Crear | `src/main/java/com/smartcommerce/order/dto/request/CreateOrderRequest.java` |
| Crear | `src/main/java/com/smartcommerce/order/dto/request/UpdateOrderStatusRequest.java` |
| Crear | `src/main/java/com/smartcommerce/order/dto/response/OrderItemResponseDTO.java` |
| Crear | `src/main/java/com/smartcommerce/order/dto/response/OrderResponseDTO.java` |
| Crear | `src/main/java/com/smartcommerce/order/dto/response/OrderStatusHistoryDTO.java` |
| Crear | `src/main/java/com/smartcommerce/order/mapper/OrderMapper.java` |
| Crear | `src/main/java/com/smartcommerce/order/service/OrderService.java` |
| Crear | `src/main/java/com/smartcommerce/order/service/impl/OrderServiceImpl.java` |
| Crear | `src/main/java/com/smartcommerce/order/controller/OrderController.java` |
| Modificar | `src/main/java/com/smartcommerce/exception/GlobalExceptionHandler.java` |
| Crear | `src/test/java/com/smartcommerce/order/service/OrderServiceTest.java` |

---

## Task 1: Enums y entidades

- [ ] Crear `src/main/java/com/smartcommerce/order/entity/OrderStatus.java`:

```java
package com.smartcommerce.order.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
```

- [ ] Crear `src/main/java/com/smartcommerce/order/entity/Order.java`:

```java
package com.smartcommerce.order.entity;

import com.smartcommerce.coupon.entity.Coupon;
import com.smartcommerce.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    // Snapshot de dirección de envío
    @Column(nullable = false)
    private String shippingStreet;

    @Column(nullable = false)
    private String shippingCity;

    @Column(nullable = false)
    private String shippingProvince;

    @Column(nullable = false)
    private String shippingPostalCode;

    @Column(nullable = false)
    private String shippingCountry;

    private String stripePaymentIntentId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
```

- [ ] Crear `src/main/java/com/smartcommerce/order/entity/OrderItem.java`:

```java
package com.smartcommerce.order.entity;

import com.smartcommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal subtotal;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/order/entity/OrderStatusHistory.java`:

```java
package com.smartcommerce.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus newStatus;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    private String changedBy;

    @PrePersist
    public void prePersist() {
        this.changedAt = LocalDateTime.now();
    }
}
```

---

## Task 2: Repositories

- [ ] Crear `src/main/java/com/smartcommerce/order/repository/OrderRepository.java`:

```java
package com.smartcommerce.order.repository;

import com.smartcommerce.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
}
```

- [ ] Crear `src/main/java/com/smartcommerce/order/repository/OrderStatusHistoryRepository.java`:

```java
package com.smartcommerce.order.repository;

import com.smartcommerce.order.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByOrderIdOrderByChangedAtAsc(Long orderId);
}
```

---

## Task 3: DTOs

- [ ] Crear `src/main/java/com/smartcommerce/order/dto/request/CreateOrderRequest.java`:

```java
package com.smartcommerce.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull
    private Long addressId;

    @NotBlank
    private String paymentMethodId;

    private String couponCode;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/order/dto/request/UpdateOrderStatusRequest.java`:

```java
package com.smartcommerce.order.dto.request;

import com.smartcommerce.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {

    @NotNull
    private OrderStatus status;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/order/dto/response/OrderItemResponseDTO.java`:

```java
package com.smartcommerce.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderItemResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/order/dto/response/OrderStatusHistoryDTO.java`:

```java
package com.smartcommerce.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderStatusHistoryDTO {
    private String previousStatus;
    private String newStatus;
    private LocalDateTime changedAt;
    private String changedBy;
}
```

- [ ] Crear `src/main/java/com/smartcommerce/order/dto/response/OrderResponseDTO.java`:

```java
package com.smartcommerce.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponseDTO {
    private Long id;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private String shippingStreet;
    private String shippingCity;
    private String shippingProvince;
    private String shippingPostalCode;
    private String shippingCountry;
    private List<OrderItemResponseDTO> items;
    private LocalDateTime createdAt;
}
```

---

## Task 4: OrderMapper

- [ ] Crear `src/main/java/com/smartcommerce/order/mapper/OrderMapper.java`:

```java
package com.smartcommerce.order.mapper;

import com.smartcommerce.order.dto.response.OrderItemResponseDTO;
import com.smartcommerce.order.dto.response.OrderResponseDTO;
import com.smartcommerce.order.dto.response.OrderStatusHistoryDTO;
import com.smartcommerce.order.entity.Order;
import com.smartcommerce.order.entity.OrderItem;
import com.smartcommerce.order.entity.OrderStatusHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    OrderResponseDTO toDTO(Order order);

    @Mapping(source = "product.id", target = "productId")
    OrderItemResponseDTO toItemDTO(OrderItem item);

    @Mapping(source = "previousStatus", target = "previousStatus", qualifiedByName = "statusToString")
    @Mapping(source = "newStatus", target = "newStatus", qualifiedByName = "statusToString")
    OrderStatusHistoryDTO toHistoryDTO(OrderStatusHistory history);

    @org.mapstruct.Named("statusToString")
    default String statusToString(com.smartcommerce.order.entity.OrderStatus status) {
        return status != null ? status.name() : null;
    }
}
```

---

## Task 5: Excepciones

- [ ] Crear `src/main/java/com/smartcommerce/exception/OrderNotFoundException.java`:

```java
package com.smartcommerce.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Pedido no encontrado con id: " + id);
    }
}
```

- [ ] Crear `src/main/java/com/smartcommerce/exception/InvalidOrderStatusException.java`:

```java
package com.smartcommerce.exception;

public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(String message) {
        super(message);
    }
}
```

- [ ] Añadir handlers en `GlobalExceptionHandler.java`:

```java
@ExceptionHandler(OrderNotFoundException.class)
public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}

@ExceptionHandler(InvalidOrderStatusException.class)
public ResponseEntity<ErrorResponse> handleInvalidOrderStatus(InvalidOrderStatusException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .message(ex.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
```

---

## Task 6: OrderService

- [ ] Crear `src/main/java/com/smartcommerce/order/service/OrderService.java`:

```java
package com.smartcommerce.order.service;

import com.smartcommerce.order.dto.request.CreateOrderRequest;
import com.smartcommerce.order.dto.request.UpdateOrderStatusRequest;
import com.smartcommerce.order.dto.response.OrderResponseDTO;
import com.smartcommerce.order.dto.response.OrderStatusHistoryDTO;
import com.smartcommerce.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(Long userId, CreateOrderRequest request);
    Page<OrderResponseDTO> getMyOrders(Long userId, Pageable pageable);
    OrderResponseDTO getMyOrderById(Long userId, Long orderId);
    void cancelOrder(Long userId, Long orderId);
    Page<OrderResponseDTO> getAllOrders(Pageable pageable);
    OrderResponseDTO getOrderByIdAdmin(Long orderId);
    OrderResponseDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequest request, String changedBy);
    List<OrderStatusHistoryDTO> getOrderHistory(Long userId, Long orderId);
}
```

---

## Task 7: OrderServiceImpl

- [ ] **Escribir el test primero** `src/test/java/com/smartcommerce/order/service/OrderServiceTest.java`:

```java
package com.smartcommerce.order.service;

import com.smartcommerce.address.entity.Address;
import com.smartcommerce.address.repository.AddressRepository;
import com.smartcommerce.cart.entity.Cart;
import com.smartcommerce.cart.entity.CartItem;
import com.smartcommerce.cart.service.CartService;
import com.smartcommerce.coupon.service.CouponService;
import com.smartcommerce.exception.AddressNotFoundException;
import com.smartcommerce.exception.InvalidOrderStatusException;
import com.smartcommerce.order.dto.request.CreateOrderRequest;
import com.smartcommerce.order.dto.request.UpdateOrderStatusRequest;
import com.smartcommerce.order.dto.response.OrderResponseDTO;
import com.smartcommerce.order.entity.Order;
import com.smartcommerce.order.entity.OrderStatus;
import com.smartcommerce.order.mapper.OrderMapper;
import com.smartcommerce.order.repository.OrderRepository;
import com.smartcommerce.order.repository.OrderStatusHistoryRepository;
import com.smartcommerce.order.service.impl.OrderServiceImpl;
import com.smartcommerce.payment.service.PaymentService;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.entity.ProductStatus;
import com.smartcommerce.product.repository.ProductRepository;
import com.smartcommerce.user.entity.Role;
import com.smartcommerce.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderStatusHistoryRepository historyRepository;
    @Mock CartService cartService;
    @Mock AddressRepository addressRepository;
    @Mock PaymentService paymentService;
    @Mock CouponService couponService;
    @Mock ProductRepository productRepository;
    @Mock OrderMapper orderMapper;
    @InjectMocks OrderServiceImpl orderService;

    private User mockUser(Long id) {
        return User.builder().id(id).email("u@test.com").role(Role.USER).active(true).build();
    }

    private Address mockAddress(Long id, Long userId) {
        User user = mockUser(userId);
        return Address.builder().id(id).user(user)
                .street("Calle Mayor 1").city("Madrid").province("Madrid")
                .postalCode("28001").country("España").isDefault(true).build();
    }

    @Test
    void createOrder_withEmptyCart_throwsException() {
        User user = mockUser(1L);
        Cart emptyCart = Cart.builder().id(1L).user(user).items(new ArrayList<>()).build();
        when(cartService.getOrCreateCart(1L)).thenReturn(emptyCart);

        CreateOrderRequest req = new CreateOrderRequest();
        req.setAddressId(1L);
        req.setPaymentMethodId("pm_test_123");

        assertThatThrownBy(() -> orderService.createOrder(1L, req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("carrito");
    }

    @Test
    void createOrder_withAddressFromAnotherUser_throwsException() {
        User user = mockUser(1L);
        Product product = Product.builder().id(1L).name("P").price(BigDecimal.TEN)
                .stock(5).status(ProductStatus.ACTIVE).build();
        CartItem item = CartItem.builder().product(product).quantity(1).build();
        Cart cart = Cart.builder().id(1L).user(user).items(List.of(item)).build();
        when(cartService.getOrCreateCart(1L)).thenReturn(cart);

        Address otherAddress = mockAddress(1L, 99L);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(otherAddress));

        CreateOrderRequest req = new CreateOrderRequest();
        req.setAddressId(1L);
        req.setPaymentMethodId("pm_test_123");

        assertThatThrownBy(() -> orderService.createOrder(1L, req))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void cancelOrder_whenNotPending_throwsException() {
        User user = mockUser(1L);
        Order order = Order.builder().id(1L).user(user).status(OrderStatus.CONFIRMED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, 1L))
                .isInstanceOf(InvalidOrderStatusException.class);
    }

    @Test
    void updateOrderStatus_adminCanChangeStatus() {
        Order order = Order.builder().id(1L)
                .user(mockUser(1L))
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .statusHistory(new ArrayList<>())
                .build();
        OrderResponseDTO dto = OrderResponseDTO.builder().id(1L).status("CONFIRMED").build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toDTO(any())).thenReturn(dto);

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(OrderStatus.CONFIRMED);

        OrderResponseDTO result = orderService.updateOrderStatus(1L, req, "admin@test.com");

        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        verify(historyRepository).save(any());
    }

    private void assertThat(String status) {
        org.assertj.core.api.Assertions.assertThat(status).isNotNull();
    }
}
```

- [ ] Ejecutar el test para verificar que falla:

```bash
./mvnw test -Dtest=OrderServiceTest
```

- [ ] Implementar `src/main/java/com/smartcommerce/order/service/impl/OrderServiceImpl.java`:

```java
package com.smartcommerce.order.service.impl;

import com.smartcommerce.address.entity.Address;
import com.smartcommerce.address.repository.AddressRepository;
import com.smartcommerce.cart.entity.Cart;
import com.smartcommerce.cart.entity.CartItem;
import com.smartcommerce.cart.service.CartService;
import com.smartcommerce.coupon.entity.Coupon;
import com.smartcommerce.coupon.entity.DiscountType;
import com.smartcommerce.coupon.service.CouponService;
import com.smartcommerce.exception.AddressNotFoundException;
import com.smartcommerce.exception.InvalidOrderStatusException;
import com.smartcommerce.exception.OrderNotFoundException;
import com.smartcommerce.order.dto.request.CreateOrderRequest;
import com.smartcommerce.order.dto.request.UpdateOrderStatusRequest;
import com.smartcommerce.order.dto.response.OrderResponseDTO;
import com.smartcommerce.order.dto.response.OrderStatusHistoryDTO;
import com.smartcommerce.order.entity.Order;
import com.smartcommerce.order.entity.OrderItem;
import com.smartcommerce.order.entity.OrderStatus;
import com.smartcommerce.order.entity.OrderStatusHistory;
import com.smartcommerce.order.mapper.OrderMapper;
import com.smartcommerce.order.repository.OrderRepository;
import com.smartcommerce.order.repository.OrderStatusHistoryRepository;
import com.smartcommerce.order.service.OrderService;
import com.smartcommerce.payment.service.PaymentService;
import com.smartcommerce.product.entity.Product;
import com.smartcommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final PaymentService paymentService;
    private final CouponService couponService;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(Long userId, CreateOrderRequest request) {
        Cart cart = cartService.getOrCreateCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("El carrito está vacío");
        }

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new AddressNotFoundException(request.getAddressId()));
        if (!address.getUser().getId().equals(userId)) {
            throw new AddressNotFoundException(request.getAddressId());
        }

        BigDecimal subtotal = calculateSubtotal(cart);
        BigDecimal discount = BigDecimal.ZERO;
        Coupon coupon = null;

        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            coupon = couponService.getValidCouponByCode(request.getCouponCode());
            discount = calculateDiscount(coupon, subtotal);
        }

        BigDecimal total = subtotal.subtract(discount).max(BigDecimal.ZERO);

        String paymentIntentId = paymentService.createAndConfirmPaymentIntent(
                total, request.getPaymentMethodId(), "eur");

        Order order = Order.builder()
                .user(com.smartcommerce.user.entity.User.builder().id(userId).build())
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .discount(discount)
                .coupon(coupon)
                .shippingStreet(address.getStreet())
                .shippingCity(address.getCity())
                .shippingProvince(address.getProvince())
                .shippingPostalCode(address.getPostalCode())
                .shippingCountry(address.getCountry())
                .stripePaymentIntentId(paymentIntentId)
                .build();

        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            BigDecimal itemSubtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
            return OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .subtotal(itemSubtotal)
                    .build();
        }).toList();

        order.getItems().addAll(orderItems);
        OrderStatusHistory initialHistory = OrderStatusHistory.builder()
                .order(order)
                .newStatus(OrderStatus.PENDING)
                .changedBy("system")
                .build();
        order.getStatusHistory().add(initialHistory);

        Order saved = orderRepository.save(order);

        if (coupon != null) couponService.incrementUsage(coupon);
        cartService.clearCart(userId);

        return orderMapper.toDTO(saved);
    }

    @Override
    public Page<OrderResponseDTO> getMyOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(orderMapper::toDTO);
    }

    @Override
    public OrderResponseDTO getMyOrderById(Long userId, Long orderId) {
        Order order = getOrderOrThrow(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new OrderNotFoundException(orderId);
        }
        return orderMapper.toDTO(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = getOrderOrThrow(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new OrderNotFoundException(orderId);
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException(
                    "Solo se puede cancelar un pedido en estado PENDING");
        }
        changeStatus(order, OrderStatus.CANCELLED, "user:" + userId);
    }

    @Override
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toDTO);
    }

    @Override
    public OrderResponseDTO getOrderByIdAdmin(Long orderId) {
        return orderMapper.toDTO(getOrderOrThrow(orderId));
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequest request,
            String changedBy) {
        Order order = getOrderOrThrow(orderId);
        changeStatus(order, request.getStatus(), changedBy);
        return orderMapper.toDTO(orderRepository.save(order));
    }

    @Override
    public List<OrderStatusHistoryDTO> getOrderHistory(Long userId, Long orderId) {
        Order order = getOrderOrThrow(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new OrderNotFoundException(orderId);
        }
        return historyRepository.findByOrderIdOrderByChangedAtAsc(orderId).stream()
                .map(orderMapper::toHistoryDTO)
                .toList();
    }

    private void changeStatus(Order order, OrderStatus newStatus, String changedBy) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .previousStatus(order.getStatus())
                .newStatus(newStatus)
                .changedBy(changedBy)
                .build();
        order.setStatus(newStatus);
        order.getStatusHistory().add(history);
        historyRepository.save(history);
    }

    private BigDecimal calculateSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            return subtotal.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
        }
        return coupon.getDiscountValue().min(subtotal);
    }

    private Order getOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
```

- [ ] Ejecutar el test:

```bash
./mvnw test -Dtest=OrderServiceTest
```

Resultado esperado: `Tests run: 4, Failures: 0, Errors: 0`

---

## Task 8: OrderController

- [ ] Crear `src/main/java/com/smartcommerce/order/controller/OrderController.java`:

```java
package com.smartcommerce.order.controller;

import com.smartcommerce.order.dto.request.CreateOrderRequest;
import com.smartcommerce.order.dto.request.UpdateOrderStatusRequest;
import com.smartcommerce.order.dto.response.OrderResponseDTO;
import com.smartcommerce.order.dto.response.OrderStatusHistoryDTO;
import com.smartcommerce.order.service.OrderService;
import com.smartcommerce.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponseDTO> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(user.getId(), request));
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderResponseDTO>> getMyOrders(
            Authentication authentication, Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.getMyOrders(user.getId(), pageable));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> getMyOrderById(
            Authentication authentication, @PathVariable Long orderId) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.getMyOrderById(user.getId(), orderId));
    }

    @PatchMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            Authentication authentication, @PathVariable Long orderId) {
        User user = (User) authentication.getPrincipal();
        orderService.cancelOrder(user.getId(), orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders/{orderId}/history")
    public ResponseEntity<List<OrderStatusHistoryDTO>> getOrderHistory(
            Authentication authentication, @PathVariable Long orderId) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.getOrderHistory(user.getId(), orderId));
    }

    // Admin endpoints
    @GetMapping("/admin/orders")
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/admin/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderByIdAdmin(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderByIdAdmin(orderId));
    }

    @PatchMapping("/admin/orders/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        User admin = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                orderService.updateOrderStatus(orderId, request, admin.getEmail()));
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
git add src/main/java/com/smartcommerce/order/ \
        src/main/java/com/smartcommerce/exception/ \
        src/test/java/com/smartcommerce/order/
git commit -m "feat(order): add checkout flow with Stripe payment, stock deduction and order management"
```
