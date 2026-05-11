# Phase 09 — Notification (Email)

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans`.

**Goal:** Enviar un email al usuario via Gmail SMTP cuando el estado de su pedido cambia. El envío se integra en `OrderServiceImpl.changeStatus()` añadido en Phase 08.

**Architecture:** `NotificationService` → `EmailService` (Spring JavaMailSender). `OrderServiceImpl` llama a `NotificationService` en cada cambio de estado. Los emails se envían de forma síncrona.

**Pre-requisitos:** Phase 08 completada. `spring-boot-starter-mail` ya en pom.xml (Phase 01). `application.properties` con `spring.mail.*` ya configurado (Phase 01).

---

## Archivos a crear / modificar

| Acción | Archivo |
|--------|---------|
| Crear | `src/main/java/com/smartcommerce/notification/service/EmailService.java` |
| Crear | `src/main/java/com/smartcommerce/notification/service/impl/EmailServiceImpl.java` |
| Crear | `src/main/java/com/smartcommerce/notification/service/NotificationService.java` |
| Crear | `src/main/java/com/smartcommerce/notification/service/impl/NotificationServiceImpl.java` |
| Modificar | `src/main/java/com/smartcommerce/order/service/impl/OrderServiceImpl.java` |
| Crear | `src/test/java/com/smartcommerce/notification/service/NotificationServiceTest.java` |

---

## Task 1: EmailService

- [ ] Crear `src/main/java/com/smartcommerce/notification/service/EmailService.java`:

```java
package com.smartcommerce.notification.service;

public interface EmailService {
    void sendHtml(String to, String subject, String htmlBody);
}
```

- [ ] Crear `src/main/java/com/smartcommerce/notification/service/impl/EmailServiceImpl.java`:

```java
package com.smartcommerce.notification.service.impl;

import com.smartcommerce.notification.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email enviado a: {}", to);
        } catch (MessagingException e) {
            log.error("Error enviando email a {}: {}", to, e.getMessage());
        }
    }
}
```

---

## Task 2: NotificationService

- [ ] Crear `src/main/java/com/smartcommerce/notification/service/NotificationService.java`:

```java
package com.smartcommerce.notification.service;

import com.smartcommerce.order.entity.Order;
import com.smartcommerce.order.entity.OrderStatus;

public interface NotificationService {
    void notifyOrderStatusChange(Order order, OrderStatus newStatus);
}
```

---

## Task 3: NotificationServiceImpl

- [ ] **Escribir el test primero** `src/test/java/com/smartcommerce/notification/service/NotificationServiceTest.java`:

```java
package com.smartcommerce.notification.service;

import com.smartcommerce.notification.service.impl.NotificationServiceImpl;
import com.smartcommerce.order.entity.Order;
import com.smartcommerce.order.entity.OrderStatus;
import com.smartcommerce.user.entity.Role;
import com.smartcommerce.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock EmailService emailService;
    @InjectMocks NotificationServiceImpl notificationService;

    private Order buildOrder(String email, OrderStatus status) {
        User user = User.builder().id(1L).email(email).firstName("Juan")
                .role(Role.USER).active(true).build();
        return Order.builder()
                .id(42L).user(user).status(status)
                .totalAmount(BigDecimal.valueOf(99.99))
                .shippingCity("Madrid").shippingStreet("Calle Mayor 1")
                .shippingProvince("Madrid").shippingPostalCode("28001").shippingCountry("España")
                .items(new ArrayList<>()).statusHistory(new ArrayList<>())
                .discount(BigDecimal.ZERO)
                .build();
    }

    @Test
    void notifyOrderStatusChange_confirmed_sendsEmailWithSubject() {
        Order order = buildOrder("juan@test.com", OrderStatus.CONFIRMED);

        notificationService.notifyOrderStatusChange(order, OrderStatus.CONFIRMED);

        verify(emailService).sendHtml(
                eq("juan@test.com"),
                contains("confirmado"),
                anyString()
        );
    }

    @Test
    void notifyOrderStatusChange_shipped_sendsEmailWithSubject() {
        Order order = buildOrder("juan@test.com", OrderStatus.SHIPPED);

        notificationService.notifyOrderStatusChange(order, OrderStatus.SHIPPED);

        verify(emailService).sendHtml(
                eq("juan@test.com"),
                contains("camino"),
                anyString()
        );
    }

    @Test
    void notifyOrderStatusChange_cancelled_sendsEmailWithSubject() {
        Order order = buildOrder("juan@test.com", OrderStatus.CANCELLED);

        notificationService.notifyOrderStatusChange(order, OrderStatus.CANCELLED);

        verify(emailService).sendHtml(
                eq("juan@test.com"),
                contains("cancelado"),
                anyString()
        );
    }
}
```

- [ ] Ejecutar el test para verificar que falla:

```bash
./mvnw test -Dtest=NotificationServiceTest
```

- [ ] Implementar `src/main/java/com/smartcommerce/notification/service/impl/NotificationServiceImpl.java`:

```java
package com.smartcommerce.notification.service.impl;

import com.smartcommerce.notification.service.EmailService;
import com.smartcommerce.notification.service.NotificationService;
import com.smartcommerce.order.entity.Order;
import com.smartcommerce.order.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;

    @Override
    public void notifyOrderStatusChange(Order order, OrderStatus newStatus) {
        String to = order.getUser().getEmail();
        String name = order.getUser().getFirstName();
        String subject = buildSubject(newStatus);
        String body = buildHtmlBody(name, order, newStatus);
        emailService.sendHtml(to, subject, body);
    }

    private String buildSubject(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "SmartCommerce — Tu pedido ha sido confirmado";
            case SHIPPED -> "SmartCommerce — Tu pedido está en camino";
            case DELIVERED -> "SmartCommerce — Tu pedido ha sido entregado";
            case CANCELLED -> "SmartCommerce — Tu pedido ha sido cancelado";
            default -> "SmartCommerce — Actualización de tu pedido";
        };
    }

    private String buildHtmlBody(String name, Order order, OrderStatus status) {
        String message = switch (status) {
            case CONFIRMED -> "Tu pedido <strong>#" + order.getId() + "</strong> ha sido confirmado y está siendo procesado.";
            case SHIPPED -> "Tu pedido <strong>#" + order.getId() + "</strong> ha sido enviado y está en camino.";
            case DELIVERED -> "Tu pedido <strong>#" + order.getId() + "</strong> ha sido entregado. ¡Esperamos que lo disfrutes!";
            case CANCELLED -> "Tu pedido <strong>#" + order.getId() + "</strong> ha sido cancelado.";
            default -> "El estado de tu pedido <strong>#" + order.getId() + "</strong> ha cambiado a " + status.name() + ".";
        };

        return """
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                  <h2 style="color: #232f3e;">SmartCommerce</h2>
                  <p>Hola, <strong>%s</strong>.</p>
                  <p>%s</p>
                  <hr/>
                  <p style="font-size: 12px; color: #888;">Dirección de envío: %s, %s, %s</p>
                  <p style="font-size: 12px; color: #888;">Total: €%s</p>
                </body>
                </html>
                """.formatted(
                name, message,
                order.getShippingStreet(), order.getShippingCity(), order.getShippingCountry(),
                order.getTotalAmount().toPlainString()
        );
    }
}
```

- [ ] Ejecutar el test:

```bash
./mvnw test -Dtest=NotificationServiceTest
```

Resultado esperado: `Tests run: 3, Failures: 0, Errors: 0`

---

## Task 4: Integrar NotificationService en OrderServiceImpl

- [ ] Abrir `src/main/java/com/smartcommerce/order/service/impl/OrderServiceImpl.java`.

- [ ] Añadir el campo `notificationService` con `@Mock` (ya que usa `@RequiredArgsConstructor`):

```java
// En la lista de campos de OrderServiceImpl, añadir:
private final NotificationService notificationService;
```

> Añadir el import: `import com.smartcommerce.notification.service.NotificationService;`

- [ ] Modificar el método `changeStatus` para llamar al notificador DESPUÉS de guardar el historial:

```java
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
    notificationService.notifyOrderStatusChange(order, newStatus);
}
```

- [ ] Ejecutar todos los tests para verificar que nada se rompe:

```bash
./mvnw test
```

Resultado esperado: `BUILD SUCCESS`

> **Nota:** `OrderServiceTest` usa `@Mock` para sus dependencias — añadir `@Mock NotificationService notificationService` al test para que Mockito lo inyecte automáticamente sin llamada real al email.

- [ ] Si `OrderServiceTest` falla por `NullPointerException` en `notificationService`, añadir el mock:

```java
// En OrderServiceTest, añadir junto a los demás @Mock:
@Mock NotificationService notificationService;
```

---

## Task 5: Verificar y commit

- [ ] Ejecutar todos los tests:

```bash
./mvnw test
```

Resultado esperado: `BUILD SUCCESS`

- [ ] Commit:

```bash
git add src/main/java/com/smartcommerce/notification/ \
        src/main/java/com/smartcommerce/order/service/impl/OrderServiceImpl.java \
        src/test/java/com/smartcommerce/notification/ \
        src/test/java/com/smartcommerce/order/
git commit -m "feat(notification): add email notifications on order status changes via Gmail SMTP"
```
