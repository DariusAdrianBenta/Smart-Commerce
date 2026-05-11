# SmartCommerce — Implementation Plan (Master)

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Construir el backend completo de SmartCommerce (Spring Boot 4, PostgreSQL, JWT, Stripe test mode) siguiendo el spec aprobado en `docs/superpowers/specs/2026-05-11-smartcommerce-design.md`.

**Architecture:** Monolito modular con arquitectura en capas (controller → service → repository). Cada fase es un vertical slice independiente y entregable. Las fases con dependencias entre sí están marcadas explícitamente.

**Tech Stack:** Java 21 · Spring Boot 4.0.5 · Spring Security 7 · jjwt 0.12.6 · Stripe Java 25.3.0 · MapStruct 1.5.5.Final · Lombok · PostgreSQL · Springdoc OpenAPI 3.0.2 · Spring Mail (Gmail SMTP)

---

## Índice de Fases

| Fase | Archivo | Depende de | Descripción |
|------|---------|------------|-------------|
| 01 | `phase-01-security-foundation.md` | — | Dependencias, User entity, JWT, SecurityConfig, Admin seed |
| 02 | `phase-02-auth-module.md` | 01 | Register, Login, JWT response |
| 03 | `phase-03-user-management.md` | 02 | Perfil propio, gestión admin de usuarios |
| 04 | `phase-04-address-module.md` | 02 | Direcciones de envío por usuario |
| 05 | `phase-05-cart-module.md` | 02 | Carrito persistente, fusión al registrarse |
| 06 | `phase-06-coupon-module.md` | 02 | Cupones de descuento (admin crea, user aplica) |
| 07 | `phase-07-payment-stripe.md` | 02 | Stripe test mode, tarjetas de prueba |
| 08 | `phase-08-order-module.md` | 04 · 05 · 06 · 07 | Checkout, pedidos, estados, stock |
| 09 | `phase-09-notification.md` | 08 | Email al cambiar estado, historial |
| 10 | `phase-10-wishlist-module.md` | 02 | Múltiples wishlists por usuario |
| 11 | `phase-11-review-module.md` | 08 | Reseñas solo para compradores verificados |
| 12 | `phase-12-product-filters.md` | 01 | Sorting: precio, más vendidos, recientes, valorados |
| 13 | `phase-13-swagger-readme.md` | Todas | OpenAPI config, README.md para GitHub |

---

## Convenciones globales (válidas para TODAS las fases)

Cualquier agente que ejecute una fase DEBE respetar estas convenciones:

- **Base package:** `com.smartcommerce`
- **API prefix:** `/api/v1/`
- **Admin routes:** `/api/v1/admin/**` → requieren `ROLE_ADMIN`
- **Rutas públicas:** documentadas explícitamente en cada fase
- **Entidades:** nunca se exponen directamente — siempre via DTOs
- **DTOs de entrada:** validados con Jakarta Validation (`@NotBlank`, `@Email`, etc.)
- **Mapeos:** siempre con MapStruct (`@Mapper(componentModel = "spring")`)
- **Excepciones:** extienden `RuntimeException`, registradas en `GlobalExceptionHandler`
- **Commits:** conventional commits — `feat(module): description`
- **Tests:** JUnit 5 + Mockito para servicios, MockMvc para controllers
- **Lombok:** `@RequiredArgsConstructor` en services y controllers

## Estructura de módulo (patrón a seguir)

```
src/main/java/com/smartcommerce/<modulo>/
├── controller/
│   └── <Modulo>Controller.java
├── service/
│   ├── <Modulo>Service.java          ← interfaz
│   └── impl/
│       └── <Modulo>ServiceImpl.java  ← implementación
├── repository/
│   └── <Modulo>Repository.java
├── entity/
│   └── <Modulo>.java
├── dto/
│   ├── request/
│   └── response/
└── mapper/
    └── <Modulo>Mapper.java
```

## Comandos de referencia

```bash
# Levantar PostgreSQL en Docker
docker-compose up -d

# Ejecutar tests
./mvnw test

# Ejecutar la aplicación
./mvnw spring-boot:run

# Compilar sin tests
./mvnw compile -DskipTests
```

## Estado de módulos ya implementados

Los siguientes módulos ya existen y NO deben ser reescritos, solo extendidos cuando sea necesario:

- `com.smartcommerce.product` — CRUD completo con filtros y paginación
- `com.smartcommerce.category` — CRUD con jerarquía (parent/subcategories)
- `com.smartcommerce.productimage` — entidad y mapper
- `com.smartcommerce.exception.GlobalExceptionHandler` — manejador global
- `com.smartcommerce.exception.ErrorResponse` — formato de error estándar

## Formato ErrorResponse (ya existe, NO recrear)

```json
{
  "status": 404,
  "message": "Product not found with id: 5",
  "timestamp": "2026-05-11T10:30:00"
}
```

## Notas de seguridad importantes

- Spring Security 7 (incluido con Spring Boot 4) — NO usar imports de `javax.servlet`, usar `jakarta.servlet`
- El filtro JWT es `OncePerRequestFilter` (no `BasicAuthenticationFilter`)
- `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` devuelve la entidad `User` directamente
- Para proteger endpoints en el controller usar `Authentication authentication` como parámetro del método
