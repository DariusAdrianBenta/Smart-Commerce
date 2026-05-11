# SmartCommerce — Especificación de Diseño

**Fecha:** 2026-05-11
**Versión:** 1.0
**Autor:** DariusAdrianBenta
**Estado:** Aprobado

---

## 1. Visión General

SmartCommerce es un backend de e-commerce profesional inspirado en Amazon, desarrollado como proyecto personal de portfolio. El objetivo es demostrar el dominio de arquitectura backend escalable, reglas de negocio reales, seguridad con JWT, integración con pasarelas de pago y buenas prácticas de desarrollo.

El sistema gestiona dos tipos de actores: administradores con control total de la plataforma y usuarios finales que pueden navegar, comprar y gestionar sus pedidos.

---

## 2. Stack Tecnológico

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje principal |
| Spring Boot | Framework backend |
| Spring Security | Autenticación y autorización |
| JWT (JSON Web Token) | Autenticación stateless |
| PostgreSQL | Base de datos relacional |
| Docker | Contenerización de la base de datos |
| JPA / Hibernate | ORM y gestión del esquema |
| MapStruct | Mapeo entre entidades y DTOs |
| Jakarta Validation | Validación de datos de entrada |
| Springdoc OpenAPI (Swagger) | Documentación interactiva de la API |
| Stripe (modo test) | Integración de pagos simulados |
| Spring Mail + Gmail SMTP | Notificaciones por email |
| Lombok | Reducción de boilerplate |

---

## 3. Arquitectura

### 3.1 Tipo de arquitectura

Monolito modular con arquitectura en capas. Cada módulo de negocio es un paquete independiente con sus propias capas internas.

```
com.smartcommerce
├── auth/
├── user/
├── address/
├── product/
├── productimage/
├── category/
├── cart/
├── order/
├── wishlist/
├── review/
├── coupon/
├── notification/
├── payment/
├── exception/
├── config/
└── security/
```

### 3.2 Capas por módulo

```
<modulo>/
├── controller/      → Endpoints REST, delegación al servicio
├── service/         → Interfaz del servicio
│   └── impl/        → Lógica de negocio
├── repository/      → JPA Repository
├── entity/          → Entidad JPA
├── dto/
│   ├── request/     → DTOs de entrada (validados)
│   └── response/    → DTOs de salida (nunca se expone la entidad)
└── mapper/          → MapStruct mapper
```

### 3.3 Principios de desarrollo

- Los controllers son delgados: solo reciben, delegan y responden.
- La lógica de negocio vive exclusivamente en los servicios.
- Las entidades nunca se exponen directamente en la API.
- Todos los datos de entrada se validan con Jakarta Validation.
- Todos los mapeos entidad ↔ DTO se realizan con MapStruct.

### 3.4 Versionado de API

Todos los endpoints están bajo el prefijo `/api/v1/`.

---

## 4. Base de Datos

- Motor: PostgreSQL 15 en contenedor Docker.
- Gestión del esquema: `spring.jpa.hibernate.ddl-auto=update` (JPA gestiona el esquema automáticamente).
- Las relaciones entre entidades se modelan con FK explícitas y anotaciones JPA.
- Los pedidos utilizan **snapshot**: al crear un pedido, se copian el nombre y precio del producto en el item del pedido, desacoplándolo de cambios futuros en el catálogo.

---

## 5. Seguridad y Autenticación

### 5.1 Roles

| Rol | Descripción |
|---|---|
| `ADMIN` | Control total: productos, categorías, usuarios, pedidos, cupones |
| `USER` | Usuario final: compra, wishlist, reseñas, perfil |

### 5.2 Autenticación JWT

- Al hacer login, el servidor emite un **JWT con validez de 1 hora**.
- No existe refresh token. Al expirar, el usuario debe volver a iniciar sesión.
- El token se envía en cada request en el header: `Authorization: Bearer <token>`.
- Spring Security valida el token en cada request mediante un filtro dedicado.

### 5.3 Admin inicial

Al arrancar la aplicación, se comprueba si existe un administrador en la base de datos. Si no existe, se inserta automáticamente un admin con credenciales hardcodeadas definidas en `application.properties` (o variables de entorno). Este admin es la puerta de entrada para promover otros usuarios.

### 5.4 Promoción de usuarios

El ADMIN puede promover cualquier usuario a ADMIN mediante un endpoint protegido:
```
PATCH /api/v1/admin/users/{userId}/promote
```
Solo accesible con rol `ADMIN`.

---

## 6. Módulos de Negocio

### 6.1 Módulo Auth

**Endpoints:**

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Público | Registro de nuevo usuario |
| `POST` | `/api/v1/auth/login` | Público | Login, devuelve JWT |

**Datos de registro:**
- Nombre
- Apellido
- Número de teléfono
- Fecha de nacimiento
- Correo electrónico (único)
- Contraseña (almacenada con BCrypt)

**Respuesta de login:**
```json
{
  "token": "eyJhbGci...",
  "expiresIn": 3600,
  "userId": 1,
  "email": "user@example.com",
  "role": "USER"
}
```

---

### 6.2 Módulo User

**Endpoints:**

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/users/me` | USER, ADMIN | Perfil del usuario autenticado |
| `PUT` | `/api/v1/users/me` | USER, ADMIN | Actualizar perfil propio |
| `GET` | `/api/v1/admin/users` | ADMIN | Listar todos los usuarios |
| `GET` | `/api/v1/admin/users/{userId}` | ADMIN | Ver detalle de un usuario |
| `PATCH` | `/api/v1/admin/users/{userId}/deactivate` | ADMIN | Desactivar cuenta de usuario |
| `PATCH` | `/api/v1/admin/users/{userId}/promote` | ADMIN | Promover usuario a ADMIN |

**Entidad User:**
```
id, firstName, lastName, phone, birthDate, email, password (BCrypt),
role (ADMIN/USER), active (boolean), createdAt
```

---

### 6.3 Módulo Address

Cada usuario puede tener múltiples direcciones guardadas. Una de ellas está marcada como dirección por defecto.

**Endpoints:**

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/addresses` | USER | Listar mis direcciones |
| `POST` | `/api/v1/addresses` | USER | Añadir nueva dirección |
| `PUT` | `/api/v1/addresses/{id}` | USER | Editar una dirección |
| `DELETE` | `/api/v1/addresses/{id}` | USER | Eliminar una dirección |
| `PATCH` | `/api/v1/addresses/{id}/default` | USER | Marcar como dirección por defecto |

**Entidad Address:**
```
id, user (FK), street, city, province, postalCode, country,
isDefault (boolean)
```

**Regla de negocio:** Al marcar una dirección como default, se desmarca automáticamente la anterior. No puede haber más de una dirección por defecto por usuario.

---

### 6.4 Módulo Product (existente, con mejoras)

El módulo ya está implementado. Se añaden mejoras en los filtros de ordenación.

**Filtros disponibles:**
- Nombre (búsqueda parcial)
- Categoría
- Rango de precio (min/max)
- Marca
- Disponibilidad (stock > 0)

**Ordenación:**
- Precio ascendente / descendente
- Más recientes (`createdAt DESC`)
- Mejor valorados (media de estrellas de reviews)
- Más vendidos (unidades totales vendidas en pedidos DELIVERED)

**Entidad Product (ya existe):**
```
id, name, description, price, stock, status (ACTIVE/INACTIVE/OUT_OF_STOCK),
brand, createdAt, category (FK), images (OneToMany)
```

---

### 6.5 Módulo Category (existente)

Ya implementado. Categorías jerárquicas con `parent_id` auto-referencial.

---

### 6.6 Módulo ProductImage (existente, con ajustes)

Las imágenes se almacenan en el sistema de ficheros local del servidor. El backend guarda la ruta relativa del fichero en la base de datos.

**Diseñado para migración futura a S3:** el servicio de imágenes tendrá una interfaz `ImageStorageService` con una implementación `LocalImageStorageService`, de modo que en el futuro se pueda añadir `S3ImageStorageService` sin tocar la lógica de negocio.

---

### 6.7 Módulo Cart

El carrito vive en base de datos y está asociado a un usuario autenticado. El carrito anónimo (usuarios sin cuenta) es responsabilidad del frontend (localStorage) y se pierde si el usuario cierra la aplicación sin registrarse.

**Fusión al registrarse:** al crear una cuenta, el frontend envía los items almacenados en localStorage. El backend los añade al carrito del nuevo usuario, sumando cantidades si el producto ya estaba en el carrito.

**Endpoints:**

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/cart` | USER | Ver mi carrito |
| `POST` | `/api/v1/cart/items` | USER | Añadir producto al carrito |
| `PUT` | `/api/v1/cart/items/{itemId}` | USER | Actualizar cantidad de un item |
| `DELETE` | `/api/v1/cart/items/{itemId}` | USER | Eliminar item del carrito |
| `DELETE` | `/api/v1/cart` | USER | Vaciar carrito |
| `POST` | `/api/v1/cart/merge` | USER | Fusionar items del localStorage al registrarse |

**Entidades:**
```
Cart: id, user (FK OneToOne), createdAt
CartItem: id, cart (FK), product (FK), quantity
```

**Reglas de negocio:**
- No se pueden añadir al carrito productos con stock 0 o estado INACTIVE/OUT_OF_STOCK.
- La cantidad de un item no puede superar el stock disponible del producto.
- Cada usuario tiene exactamente un carrito.

---

### 6.8 Módulo Order

**Flujo de estados:**
```
PENDING → CONFIRMED → SHIPPED → DELIVERED → CANCELLED
```

- `PENDING`: pedido creado, esperando confirmación de pago.
- `CONFIRMED`: pago exitoso, el admin lo procesa.
- `SHIPPED`: enviado al cliente.
- `DELIVERED`: entregado. A partir de este estado el usuario puede dejar reseñas.
- `CANCELLED`: cancelado por el usuario o el admin.

El ADMIN puede cambiar manualmente el estado de cualquier pedido.

**Snapshot:** al crear el pedido, se copian `productName` y `unitPrice` en `OrderItem`. Si el producto cambia de precio en el futuro, los pedidos históricos no se ven afectados.

**Endpoints:**

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/v1/orders` | USER | Crear pedido (checkout) |
| `GET` | `/api/v1/orders` | USER | Mis pedidos |
| `GET` | `/api/v1/orders/{id}` | USER | Detalle de un pedido |
| `PATCH` | `/api/v1/orders/{id}/cancel` | USER | Cancelar pedido (solo si PENDING) |
| `GET` | `/api/v1/admin/orders` | ADMIN | Listar todos los pedidos |
| `GET` | `/api/v1/admin/orders/{id}` | ADMIN | Detalle de cualquier pedido |
| `PATCH` | `/api/v1/admin/orders/{id}/status` | ADMIN | Cambiar estado manualmente |

**Entidades:**
```
Order: id, user (FK), status, totalAmount, coupon (FK nullable), discount, createdAt,
       — snapshot de dirección de envío (campos copiados, no FK) —
       shippingStreet, shippingCity, shippingProvince, shippingPostalCode, shippingCountry

OrderItem: id, order (FK), productId, productName (snapshot),
           unitPrice (snapshot), quantity, subtotal
```

> El motivo del snapshot de dirección: si el usuario edita o elimina su dirección después de hacer el pedido, el historial del pedido debe conservar la dirección de entrega original.

**Checkout — flujo:**
1. El usuario hace POST `/api/v1/orders` con `addressId`, `paymentMethodId` (token de Stripe) y opcionalmente `couponCode`.
2. El backend valida stock para cada item del carrito.
3. Si hay cupón, calcula el descuento.
4. Crea el PaymentIntent en Stripe con el importe final.
5. Si Stripe confirma el pago → crea el pedido en estado `PENDING`, descuenta el stock, vacía el carrito.
6. Si Stripe rechaza el pago → devuelve error, no se crea el pedido.

---

### 6.9 Módulo Payment (Stripe)

Se utiliza el SDK de Stripe en **modo test**. No hay dinero real involucrado.

**Endpoints:**

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/payments/test-cards` | Público | Lista de tarjetas de prueba de Stripe |

**Tarjetas de prueba expuestas:**

| Número | Marca | Comportamiento |
|---|---|---|
| `4242 4242 4242 4242` | Visa | Pago exitoso |
| `4000 0000 0000 0002` | Visa | Tarjeta rechazada |
| `4000 0025 0000 3155` | Visa | Requiere autenticación 3D Secure |
| `4000 0000 0000 9995` | Visa | Fondos insuficientes |

Todas las tarjetas usan cualquier fecha futura y cualquier CVV de 3 dígitos.

El endpoint `GET /api/v1/payments/test-cards` está documentado en Swagger con ejemplos completos.

---

### 6.10 Módulo Wishlist

Cada usuario autenticado puede tener múltiples listas de deseos con nombre personalizado.

**Endpoints:**

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/wishlists` | USER | Mis listas de deseos |
| `POST` | `/api/v1/wishlists` | USER | Crear nueva lista |
| `PUT` | `/api/v1/wishlists/{id}` | USER | Renombrar lista |
| `DELETE` | `/api/v1/wishlists/{id}` | USER | Eliminar lista |
| `POST` | `/api/v1/wishlists/{id}/products/{productId}` | USER | Añadir producto a lista |
| `DELETE` | `/api/v1/wishlists/{id}/products/{productId}` | USER | Eliminar producto de lista |

**Entidades:**
```
Wishlist: id, user (FK), name, createdAt
WishlistProduct: wishlist (FK), product (FK)  [ManyToMany join table]
```

---

### 6.11 Módulo Review

Solo usuarios autenticados que tengan el producto en un pedido con estado `DELIVERED` pueden dejar reseña. Un usuario solo puede dejar una reseña por producto.

**Endpoints:**

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/products/{productId}/reviews` | Público | Ver reseñas de un producto |
| `POST` | `/api/v1/products/{productId}/reviews` | USER | Crear reseña (solo compradores) |
| `PUT` | `/api/v1/reviews/{id}` | USER | Editar mi reseña |
| `DELETE` | `/api/v1/reviews/{id}` | USER | Eliminar mi reseña |

**Entidad Review:**
```
id, user (FK), product (FK), rating (1-5), comment, createdAt
```
Constraint unique: `(user_id, product_id)` — una reseña por usuario por producto.

---

### 6.12 Módulo Coupon

El ADMIN crea cupones que los usuarios aplican al hacer checkout.

**Endpoints:**

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/admin/coupons` | ADMIN | Listar todos los cupones |
| `POST` | `/api/v1/admin/coupons` | ADMIN | Crear cupón |
| `PUT` | `/api/v1/admin/coupons/{id}` | ADMIN | Editar cupón |
| `DELETE` | `/api/v1/admin/coupons/{id}` | ADMIN | Eliminar cupón |
| `GET` | `/api/v1/coupons/validate/{code}` | USER | Validar cupón antes del checkout |

**Entidad Coupon:**
```
id, code (único), discountType (PERCENTAGE / FIXED_AMOUNT),
discountValue, expiresAt, active (boolean), usageLimit (nullable),
usageCount
```

**Reglas de negocio:**
- Un cupón expirado o inactivo no puede aplicarse.
- Si tiene `usageLimit`, al alcanzarse se desactiva automáticamente.
- El descuento de tipo `PERCENTAGE` se aplica sobre el subtotal del pedido.
- El descuento de tipo `FIXED_AMOUNT` se resta directamente, con un mínimo de 0€.

---

### 6.13 Módulo Notification

Al cambiar el estado de un pedido, el sistema envía un email al usuario y registra el evento en base de datos.

**Proveedor:** Gmail SMTP mediante Spring Mail (`spring.mail.*` en `application.properties`).

**Eventos que generan notificación:**
- `PENDING` → `CONFIRMED`: "Tu pedido ha sido confirmado."
- `CONFIRMED` → `SHIPPED`: "Tu pedido está en camino."
- `SHIPPED` → `DELIVERED`: "Tu pedido ha sido entregado."
- `* → CANCELLED`: "Tu pedido ha sido cancelado."

**Endpoints:**

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/orders/{id}/history` | USER | Historial de estados de un pedido |

**Entidad OrderStatusHistory:**
```
id, order (FK), previousStatus, newStatus, changedAt, changedBy (email del actor)
```

---

## 7. Manejo de Errores

Se mantiene el `GlobalExceptionHandler` existente. Se añaden nuevas excepciones por módulo:

| Excepción | HTTP | Cuándo |
|---|---|---|
| `UserNotFoundException` | 404 | Usuario no encontrado |
| `EmailAlreadyExistsException` | 409 | Email duplicado en registro |
| `CartItemNotFoundException` | 404 | Item no existe en el carrito |
| `InsufficientStockException` | 409 | Stock insuficiente al añadir al carrito o hacer checkout |
| `OrderNotFoundException` | 404 | Pedido no encontrado |
| `InvalidOrderStatusException` | 400 | Transición de estado inválida |
| `CouponNotFoundException` | 404 | Cupón no encontrado |
| `CouponExpiredException` | 400 | Cupón expirado o inactivo |
| `ReviewAlreadyExistsException` | 409 | Usuario ya dejó reseña de ese producto |
| `ReviewNotAllowedException` | 403 | Usuario no ha comprado el producto |
| `PaymentFailedException` | 402 | Stripe rechazó el pago |
| `AddressNotFoundException` | 404 | Dirección no encontrada |
| `WishlistNotFoundException` | 404 | Lista de deseos no encontrada |

Formato de error estándar (ya existente en `ErrorResponse`):
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 5",
  "timestamp": "2026-05-11T10:30:00"
}
```

---

## 8. Documentación

### 8.1 Swagger / OpenAPI

- Integrado con Springdoc OpenAPI.
- UI accesible en `/swagger-ui.html`.
- Autenticación JWT integrada: el reclutador puede hacer login, copiar el token y usarlo directamente en Swagger.
- Todos los endpoints documentados con descripción, parámetros y ejemplos de request/response.
- El endpoint `GET /api/v1/payments/test-cards` incluye las tarjetas de prueba de Stripe con sus comportamientos.

### 8.2 README.md

El README de GitHub incluirá:
- Descripción del proyecto y tecnologías usadas.
- Diagrama de arquitectura (o descripción textual).
- Instrucciones para levantar el proyecto con Docker.
- Cómo autenticarse en Swagger.
- Lista de tarjetas de prueba de Stripe.
- Lista de endpoints principales por módulo.

---

## 9. Docker

### Infraestructura actual

El `docker-compose.yml` levanta PostgreSQL. La aplicación Spring Boot se ejecuta localmente durante el desarrollo.

### Evolución futura

En una fase posterior, se contenerizará también el backend (y el frontend cuando exista), con un `docker-compose.yml` que incluya:
- Contenedor PostgreSQL
- Contenedor backend Spring Boot
- Contenedor frontend (a definir en su momento)

---

## 10. Módulos — Orden de Implementación Recomendado

El orden respeta las dependencias entre módulos:

1. **Auth / User** — base de todo lo demás (seguridad, JWT)
2. **Address** — necesaria antes de Orders
3. **Cart** — necesaria antes del checkout
4. **Payment (Stripe)** — necesaria en el checkout
5. **Coupon** — aplica en el checkout
6. **Order** — depende de Cart, Address, Payment, Coupon
7. **Notification** — se activa desde Order
8. **Wishlist** — independiente
9. **Review** — depende de Order (verificar compra)
10. **Mejoras de filtros en Product** — mejoras sobre módulo existente
11. **Swagger completo + README** — cierre del proyecto

---

## 11. Resumen de Entidades y Relaciones

```
User ──────────────── Address (OneToMany)
User ──────────────── Cart (OneToOne)
Cart ──────────────── CartItem (OneToMany)
CartItem ──────────── Product (ManyToOne)
User ──────────────── Order (OneToMany)
Order ─────────────── OrderItem (OneToMany)
Order ─────────────── dirección (campos copiados como snapshot, sin FK)
Order ─────────────── Coupon (ManyToOne, nullable)
Order ─────────────── OrderStatusHistory (OneToMany)
OrderItem ─────────── Product (ManyToOne, con snapshot de nombre y precio)
User ──────────────── Wishlist (OneToMany)
Wishlist ──────────── Product (ManyToMany via WishlistProduct)
User ──────────────── Review (OneToMany)
Review ─────────────── Product (ManyToOne)
Product ────────────── Category (ManyToOne)
Product ────────────── ProductImage (OneToMany)
Category ───────────── Category (ManyToOne self-referential, parent)
```
