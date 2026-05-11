# Phase 13 — Swagger/OpenAPI Config + README

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or `superpowers:executing-plans`.

**Goal:** Configurar Swagger UI con autenticación JWT integrada (el reclutador puede hacer login y usar el token directamente en la UI). Añadir anotaciones de documentación en los controllers principales. Crear el README.md para GitHub con instrucciones completas.

**Architecture:** `OpenApiConfig` configura el esquema JWT en la UI de Swagger. Springdoc 3.0.2 ya está en el pom.xml. El README explica cómo levantar el proyecto, autenticarse y usar las tarjetas de prueba de Stripe.

**Pre-requisitos:** Todas las fases anteriores completadas.

---

## Archivos a crear / modificar

| Acción | Archivo |
|--------|---------|
| Crear | `src/main/java/com/smartcommerce/config/OpenApiConfig.java` |
| Modificar | `src/main/resources/application.properties` |
| Crear | `README.md` |

---

## Task 1: OpenApiConfig

- [ ] Crear `src/main/java/com/smartcommerce/config/OpenApiConfig.java`:

```java
package com.smartcommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartCommerce API")
                        .version("1.0.0")
                        .description("""
                                Backend de e-commerce inspirado en Amazon.
                                
                                **Cómo autenticarse:**
                                1. Usa `POST /api/v1/auth/login` con las credenciales del admin (admin@smartcommerce.com / Admin1234!)
                                2. Copia el `token` de la respuesta
                                3. Haz clic en el botón **Authorize** (arriba a la derecha)
                                4. Pega el token en el campo **Value** y haz clic en **Authorize**
                                
                                **Tarjetas de prueba Stripe:** `GET /api/v1/payments/test-cards`
                                """)
                        .contact(new Contact()
                                .name("DariusAdrianBenta")
                                .url("https://github.com/DariusAdrianBenta")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Introduce el JWT obtenido en POST /api/v1/auth/login")));
    }
}
```

---

## Task 2: Configurar Swagger en application.properties

- [ ] Añadir al final de `src/main/resources/application.properties`:

```properties
# Swagger / OpenAPI
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.try-it-out-enabled=true
```

---

## Task 3: Verificar Swagger UI

- [ ] Arrancar la aplicación:

```bash
./mvnw spring-boot:run
```

- [ ] Abrir en el navegador:

```
http://localhost:8080/swagger-ui.html
```

Resultado esperado: Swagger UI cargado con el título "SmartCommerce API", botón **Authorize** visible en la esquina superior derecha, y todos los endpoints agrupados por tags.

- [ ] Hacer login desde Swagger UI:
  1. Expandir `POST /api/v1/auth/login`
  2. Click **Try it out**
  3. Introducir: `{ "email": "admin@smartcommerce.com", "password": "Admin1234!" }`
  4. Click **Execute**
  5. Copiar el `token` de la respuesta
  6. Click **Authorize** (arriba a la derecha)
  7. Pegar el token → **Authorize**

- [ ] Verificar que el endpoint protegido funciona:
  1. Expandir `GET /api/v1/users/me`
  2. Click **Try it out** → **Execute**
  3. Resultado esperado: `200 OK` con los datos del admin

---

## Task 4: Crear README.md

- [ ] Crear `README.md` en la raíz del proyecto:

```markdown
# SmartCommerce API

Backend de e-commerce profesional inspirado en Amazon, desarrollado con Java 21, Spring Boot 4 y PostgreSQL.

## Stack Tecnológico

| Tecnología | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.5 |
| Spring Security | 7 (JWT stateless) |
| PostgreSQL | 15 |
| Docker | Compose |
| Stripe | Test mode |
| MapStruct | 1.5.5 |
| Springdoc OpenAPI | 3.0.2 |

## Arquitectura

Monolito modular con arquitectura en capas. Cada módulo de negocio es un paquete independiente con controller, service, repository, entity, dto y mapper.

```
com.smartcommerce
├── auth/        → Registro y login JWT
├── user/        → Perfil y gestión de usuarios
├── address/     → Direcciones de envío
├── product/     → Catálogo con filtros y paginación
├── category/    → Categorías jerárquicas
├── cart/        → Carrito persistente
├── order/       → Pedidos y checkout con Stripe
├── coupon/      → Cupones de descuento
├── wishlist/    → Listas de deseos
├── review/      → Reseñas de compradores verificados
├── notification/→ Emails de cambio de estado
└── payment/     → Integración Stripe test mode
```

## Cómo levantar el proyecto

### Pre-requisitos

- Java 21
- Docker y Docker Compose
- Maven (o usar el wrapper `./mvnw`)

### 1. Clonar el repositorio

```bash
git clone https://github.com/DariusAdrianBenta/smartcommerce.git
cd smartcommerce
```

### 2. Configurar variables de entorno

Editar `src/main/resources/application.properties` y reemplazar:

```properties
stripe.secret.key=sk_test_TU_CLAVE_AQUI
stripe.publishable.key=pk_test_TU_CLAVE_AQUI
spring.mail.username=tu@gmail.com
spring.mail.password=tu-password-de-aplicacion
```

> Para obtener claves de Stripe: https://dashboard.stripe.com/test/apikeys  
> Para Gmail: activa la verificación en dos pasos y genera una "Contraseña de aplicación"

### 3. Levantar PostgreSQL

```bash
docker-compose up -d
```

### 4. Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

La aplicación arranca en `http://localhost:8080`.

Al iniciar, se crea automáticamente el usuario administrador:
- **Email:** `admin@smartcommerce.com`
- **Password:** `Admin1234!`

---

## Documentación interactiva (Swagger UI)

```
http://localhost:8080/swagger-ui.html
```

### Cómo autenticarse en Swagger

1. `POST /api/v1/auth/login` → introduce las credenciales del admin
2. Copia el `token` de la respuesta
3. Click en **Authorize** (botón en la esquina superior derecha)
4. Pega el token → **Authorize**

---

## Tarjetas de prueba de Stripe

Obtén la lista completa en: `GET /api/v1/payments/test-cards`

| Número | Comportamiento |
|---|---|
| `4242 4242 4242 4242` | Pago exitoso |
| `4000 0000 0000 0002` | Tarjeta rechazada |
| `4000 0025 0000 3155` | Requiere 3D Secure |
| `4000 0000 0000 9995` | Fondos insuficientes |

Usa cualquier fecha futura y cualquier CVV de 3 dígitos.

---

## Endpoints principales

### Autenticación (Público)
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/auth/register` | Registrar nuevo usuario |
| POST | `/api/v1/auth/login` | Login → devuelve JWT |

### Productos (Público para GET)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/products` | Listar con filtros y paginación |
| GET | `/api/v1/products/{id}` | Detalle de producto |
| GET | `/api/v1/products/{id}/reviews` | Reseñas del producto |

### Carrito (Autenticado)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/cart` | Ver mi carrito |
| POST | `/api/v1/cart/items` | Añadir producto |
| POST | `/api/v1/cart/merge` | Fusionar carrito anónimo al registrarse |

### Pedidos (Autenticado)
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/orders` | Crear pedido (checkout con Stripe) |
| GET | `/api/v1/orders` | Mis pedidos |
| GET | `/api/v1/orders/{id}/history` | Historial de estados |

### Admin
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/admin/users` | Listar todos los usuarios |
| PATCH | `/api/v1/admin/users/{id}/promote` | Promover a ADMIN |
| GET | `/api/v1/admin/orders` | Listar todos los pedidos |
| PATCH | `/api/v1/admin/orders/{id}/status` | Cambiar estado manualmente |
| POST | `/api/v1/admin/coupons` | Crear cupón de descuento |

---

## Ejecutar tests

```bash
./mvnw test
```

---

## Autor

**Darius Adrian Benta** — [GitHub](https://github.com/DariusAdrianBenta)
```

---

## Task 5: Verificar y commit final

- [ ] Ejecutar todos los tests:

```bash
./mvnw test
```

Resultado esperado: `BUILD SUCCESS`

- [ ] Commit:

```bash
git add src/main/java/com/smartcommerce/config/OpenApiConfig.java \
        src/main/resources/application.properties \
        README.md
git commit -m "docs: add Swagger/OpenAPI config with JWT auth and GitHub README"
```

---

## Verificación final del proyecto completo

- [ ] Levantar Docker y la app:

```bash
docker-compose up -d && ./mvnw spring-boot:run
```

- [ ] Abrir Swagger UI: `http://localhost:8080/swagger-ui.html`

- [ ] Flujo completo de prueba:
  1. Login como admin → obtener JWT
  2. Crear una categoría y un producto (admin)
  3. Registrar un usuario nuevo
  4. Login como usuario → obtener JWT
  5. Añadir dirección de envío
  6. Añadir producto al carrito
  7. Hacer checkout con tarjeta `4242 4242 4242 4242`
  8. Verificar que el pedido se creó en estado `PENDING`
  9. Como admin, cambiar el estado a `CONFIRMED`
  10. Verificar que llega el email (si Gmail SMTP está configurado)
  11. Cambiar a `DELIVERED`
  12. Como usuario, dejar una reseña del producto comprado
  13. Ver la reseña en `GET /api/v1/products/{id}/reviews`
```
