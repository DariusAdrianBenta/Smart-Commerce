# Phase 07 — Payment + Stripe

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans`.

**Goal:** Integrar Stripe en modo test. Exponer el endpoint `GET /api/v1/payments/test-cards` público. Crear `PaymentService` que genera PaymentIntents — será llamado por `OrderService` en Phase 08.

**Architecture:** `PaymentController` → `StripePaymentServiceImpl`. Stripe se configura con `stripe.secret.key` de `application.properties`. El endpoint de test-cards es estático (no llama a Stripe).

**Pre-requisitos:** Phase 01 completada (dependencia `stripe-java` en pom.xml). Phase 02 completada (JWT).

---

## Archivos a crear

| Acción | Archivo |
|--------|---------|
| Crear | `src/main/java/com/smartcommerce/payment/dto/TestCardDTO.java` |
| Crear | `src/main/java/com/smartcommerce/payment/service/PaymentService.java` |
| Crear | `src/main/java/com/smartcommerce/payment/service/impl/StripePaymentServiceImpl.java` |
| Crear | `src/main/java/com/smartcommerce/payment/controller/PaymentController.java` |
| Modificar | `src/main/java/com/smartcommerce/exception/GlobalExceptionHandler.java` |
| Crear | `src/test/java/com/smartcommerce/payment/service/StripePaymentServiceTest.java` |

---

## Task 1: TestCardDTO

- [ ] Crear `src/main/java/com/smartcommerce/payment/dto/TestCardDTO.java`:

```java
package com.smartcommerce.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TestCardDTO {
    private String number;
    private String brand;
    private String expiry;
    private String cvc;
    private String behavior;
}
```

---

## Task 2: Excepción de pago

- [ ] Crear `src/main/java/com/smartcommerce/exception/PaymentFailedException.java`:

```java
package com.smartcommerce.exception;

public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super("Pago rechazado: " + message);
    }
}
```

- [ ] Añadir handler en `GlobalExceptionHandler.java`:

```java
@ExceptionHandler(PaymentFailedException.class)
public ResponseEntity<ErrorResponse> handlePaymentFailed(PaymentFailedException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.PAYMENT_REQUIRED.value())
            .message(ex.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(error);
}
```

---

## Task 3: PaymentService

- [ ] Crear `src/main/java/com/smartcommerce/payment/service/PaymentService.java`:

```java
package com.smartcommerce.payment.service;

import java.math.BigDecimal;
import java.util.List;

import com.smartcommerce.payment.dto.TestCardDTO;

public interface PaymentService {
    String createAndConfirmPaymentIntent(BigDecimal amount, String paymentMethodId, String currency);
    List<TestCardDTO> getTestCards();
}
```

---

## Task 4: StripePaymentServiceImpl

- [ ] **Escribir el test primero** `src/test/java/com/smartcommerce/payment/service/StripePaymentServiceTest.java`:

```java
package com.smartcommerce.payment.service;

import com.smartcommerce.payment.dto.TestCardDTO;
import com.smartcommerce.payment.service.impl.StripePaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StripePaymentServiceTest {

    private StripePaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new StripePaymentServiceImpl();
        ReflectionTestUtils.setField(paymentService, "stripeSecretKey", "sk_test_fake_key");
    }

    @Test
    void getTestCards_returnsNonEmptyList() {
        List<TestCardDTO> cards = paymentService.getTestCards();
        assertThat(cards).isNotEmpty();
    }

    @Test
    void getTestCards_containsSuccessCard() {
        List<TestCardDTO> cards = paymentService.getTestCards();
        assertThat(cards).anyMatch(c -> c.getNumber().equals("4242424242424242"));
    }

    @Test
    void getTestCards_eachCardHasAllFields() {
        List<TestCardDTO> cards = paymentService.getTestCards();
        for (TestCardDTO card : cards) {
            assertThat(card.getNumber()).isNotBlank();
            assertThat(card.getBrand()).isNotBlank();
            assertThat(card.getBehavior()).isNotBlank();
        }
    }
}
```

- [ ] Ejecutar el test para verificar que falla:

```bash
./mvnw test -Dtest=StripePaymentServiceTest
```

- [ ] Implementar `src/main/java/com/smartcommerce/payment/service/impl/StripePaymentServiceImpl.java`:

```java
package com.smartcommerce.payment.service.impl;

import com.smartcommerce.exception.PaymentFailedException;
import com.smartcommerce.payment.dto.TestCardDTO;
import com.smartcommerce.payment.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class StripePaymentServiceImpl implements PaymentService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public String createAndConfirmPaymentIntent(BigDecimal amount, String paymentMethodId, String currency) {
        try {
            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency.toLowerCase())
                    .setPaymentMethod(paymentMethodId)
                    .setConfirm(true)
                    .setReturnUrl("http://localhost:8080")
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            if (!"succeeded".equals(intent.getStatus())) {
                throw new PaymentFailedException("Estado del pago: " + intent.getStatus());
            }

            return intent.getId();
        } catch (StripeException e) {
            log.error("Stripe error: {}", e.getMessage());
            throw new PaymentFailedException(e.getMessage());
        }
    }

    @Override
    public List<TestCardDTO> getTestCards() {
        return List.of(
            TestCardDTO.builder()
                .number("4242424242424242")
                .brand("Visa")
                .expiry("Cualquier fecha futura")
                .cvc("Cualquier 3 dígitos")
                .behavior("Pago exitoso")
                .build(),
            TestCardDTO.builder()
                .number("4000000000000002")
                .brand("Visa")
                .expiry("Cualquier fecha futura")
                .cvc("Cualquier 3 dígitos")
                .behavior("Tarjeta rechazada")
                .build(),
            TestCardDTO.builder()
                .number("4000002500003155")
                .brand("Visa")
                .expiry("Cualquier fecha futura")
                .cvc("Cualquier 3 dígitos")
                .behavior("Requiere autenticación 3D Secure")
                .build(),
            TestCardDTO.builder()
                .number("4000000000009995")
                .brand("Visa")
                .expiry("Cualquier fecha futura")
                .cvc("Cualquier 3 dígitos")
                .behavior("Fondos insuficientes")
                .build()
        );
    }
}
```

- [ ] Ejecutar el test:

```bash
./mvnw test -Dtest=StripePaymentServiceTest
```

Resultado esperado: `Tests run: 3, Failures: 0, Errors: 0`

---

## Task 5: PaymentController

- [ ] Crear `src/main/java/com/smartcommerce/payment/controller/PaymentController.java`:

```java
package com.smartcommerce.payment.controller;

import com.smartcommerce.payment.dto.TestCardDTO;
import com.smartcommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/test-cards")
    public ResponseEntity<List<TestCardDTO>> getTestCards() {
        return ResponseEntity.ok(paymentService.getTestCards());
    }
}
```

> **Nota:** Este endpoint es público (configurado en `SecurityConfig` de Phase 01): `requestMatchers(HttpMethod.GET, "/api/v1/payments/test-cards").permitAll()`

---

## Task 6: Verificar con curl

- [ ] Verificar que el endpoint es accesible sin token:

```bash
curl http://localhost:8080/api/v1/payments/test-cards
```

Resultado esperado: array JSON con 4 tarjetas de prueba, status `200 OK`.

---

## Task 7: Verificar y commit

- [ ] Ejecutar todos los tests:

```bash
./mvnw test
```

Resultado esperado: `BUILD SUCCESS`

- [ ] Commit:

```bash
git add src/main/java/com/smartcommerce/payment/ \
        src/main/java/com/smartcommerce/exception/ \
        src/test/java/com/smartcommerce/payment/
git commit -m "feat(payment): add Stripe integration and public test-cards endpoint"
```
