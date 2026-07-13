# Guía: crear categorías y productos en SmartCommerce

Cómo dar de alta **categorías** y **productos**, por tres vías: **Swagger**,
**Postman** y **base de datos (SQL)**.

---

## 0. Conceptos previos: token y autorización (IMPORTANTE)

La API usa **JWT** (JSON Web Token). Funciona así:

1. Te **autenticas** (login) con email y contraseña → la API te devuelve un **token**.
2. Ese token es como una **pulsera de acceso**: demuestra quién eres y **qué rol** tienes (`USER` o `ADMIN`).
3. Para las acciones **de administrador** (crear categorías/productos) necesitas un token de un usuario **ADMIN**.
4. El token se envía en cada petición en la cabecera:
   ```
   Authorization: Bearer <token>
   ```
5. El token **caduca** (1 hora). Si caduca, vuelves a hacer login para obtener otro.

**Usuario administrador (semilla):**
- Email: `admin@smartcommerce.com`
- Contraseña: `Admin1234!`

**Endpoints que vamos a usar:**

| Acción | Método y ruta | Requiere |
|---|---|---|
| Login (obtener token) | `POST /api/v1/auth/login` | — (público) |
| Crear categoría | `POST /api/v1/categories/admin` | token ADMIN |
| Crear producto | `POST /api/v1/admin/products` | token ADMIN |

**Campos del cuerpo (JSON):**

- **Categoría:** `name` (obligatorio), `parentId` (opcional, para subcategorías).
- **Producto:** `name` (obligatorio), `price` (obligatorio, > 0), `stock` (obligatorio, ≥ 0), `categoryId` (obligatorio), `description` (opcional), `brand` (opcional), `imageUrls` (opcional, lista de URLs).

> El backend genera solo el `slug` y el estado del producto. Tú no los mandas.

**Requisito previo (para Swagger y Postman):** tener arrancados la base de datos y el backend:
```bash
docker compose up -d        # PostgreSQL
./mvnw spring-boot:run      # Backend en http://localhost:8080
```

---

## 1. Por Swagger (la forma más visual — ideal para demos)

1. Con el backend arrancado, abre en el navegador:
   **http://localhost:8080/swagger-ui.html**
2. **Obtener el token:** busca `auth-controller` → `POST /api/v1/auth/login` → **Try it out**.
   Pega el cuerpo y pulsa **Execute**:
   ```json
   { "email": "admin@smartcommerce.com", "password": "Admin1234!" }
   ```
   En la respuesta, **copia el valor de `token`**.
3. **Autorizarte:** arriba a la derecha pulsa el botón **Authorize** 🔒.
   En el campo escribe `Bearer <token>` (pega tu token) y pulsa **Authorize** → **Close**.
   *(A partir de ahora Swagger manda el token en todas las peticiones.)*
4. **Crear categoría:** `category-controller` → `POST /api/v1/categories/admin` → **Try it out**:
   ```json
   { "name": "Juguetes" }
   ```
   **Execute**. Respuesta `200/201` con el `id` de la categoría. **Apunta ese `id`.**
5. **Crear producto:** `product-controller` → `POST /api/v1/admin/products` → **Try it out**
   (usa el `id` de categoría del paso anterior):
   ```json
   {
     "name": "Peluche Oso",
     "price": 14.99,
     "stock": 50,
     "brand": "CuddleCo",
     "categoryId": 10,
     "imageUrls": ["https://picsum.photos/seed/oso/500/500"]
   }
   ```
   **Execute**. Respuesta `201 Created` con el producto creado.

---

## 2. Por Postman

1. **Login:** nueva petición **POST** a `http://localhost:8080/api/v1/auth/login`.
   - Pestaña **Body** → **raw** → **JSON**:
     ```json
     { "email": "admin@smartcommerce.com", "password": "Admin1234!" }
     ```
   - **Send** → copia el `token` de la respuesta.
2. **Crear categoría:** nueva petición **POST** a `http://localhost:8080/api/v1/categories/admin`.
   - Pestaña **Authorization** → Type **Bearer Token** → pega el token.
     *(O en **Headers**: `Authorization` = `Bearer <token>`.)*
   - **Body** → raw → JSON:
     ```json
     { "name": "Juguetes" }
     ```
   - **Send** → apunta el `id` de la categoría.
3. **Crear producto:** nueva petición **POST** a `http://localhost:8080/api/v1/admin/products`.
   - **Authorization** → Bearer Token (el mismo token).
   - **Body** → raw → JSON:
     ```json
     {
       "name": "Peluche Oso",
       "price": 14.99,
       "stock": 50,
       "brand": "CuddleCo",
       "categoryId": 10,
       "imageUrls": ["https://picsum.photos/seed/oso/500/500"]
     }
     ```
   - **Send** → `201 Created`.

> Truco: en Postman puedes guardar el token en una **variable de entorno** (`{{token}}`) y reutilizarlo en todas las peticiones.

---

## 3. Por base de datos (SQL directo)

Solo para casos puntuales: **te saltas la lógica de la app**, así que debes rellenar
a mano el `slug`, `active` y el `status` (por eso surgió el bug del slug).

1. Entra en el contenedor de PostgreSQL:
   ```bash
   docker exec -it smartcommerce-db psql -U postgres -d smartcommerce
   ```
2. **Crear categoría** (hay que poner `slug` y `active` tú mismo):
   ```sql
   INSERT INTO categories (name, slug, active)
   VALUES ('Juguetes', 'juguetes', true);
   ```
3. **Ver el id de la categoría** (para el producto):
   ```sql
   SELECT id, name FROM categories;
   ```
4. **Crear producto** (hay que poner `status` y `created_at`):
   ```sql
   INSERT INTO products (name, price, stock, status, category_id, created_at)
   VALUES ('Peluche Oso', 14.99, 50, 'ACTIVE', 10, NOW());
   ```
   Valores válidos de `status`: `ACTIVE`, `OUT_OF_STOCK`, `DISABLED`.
5. Salir de psql: `\q`

> No recomendado como forma habitual: la API es la vía correcta porque aplica
> validaciones y reglas de negocio automáticamente.

---

## Resumen rápido

| Vía | Cuándo usarla |
|---|---|
| **Swagger** | Demos y pruebas rápidas con interfaz visual. |
| **Postman** | Trabajo repetido, colecciones, variables de entorno. |
| **SQL** | Arreglos puntuales directos en la BD (con cuidado). |

En las tres, la idea es la misma: **login → token → usar el token en las
peticiones de admin**. La única que no usa token es el SQL directo (pero pierdes
las validaciones de la app).
