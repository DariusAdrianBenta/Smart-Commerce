# SmartCommerce Frontend – Especificación de Diseño

**Fecha:** 2026-07-12
**Estado:** Aprobado
**Ámbito:** Cliente web (SPA) para la API REST de SmartCommerce

---

## 1. Objetivo

Desarrollar el cliente web de SmartCommerce: una **Single Page Application (SPA)** en React que consume la API REST del backend. La aplicación ofrece autenticación de usuarios, navegación por el catálogo de productos y gestión del carrito de compra, con una interfaz limpia y responsive.

**Modelo de navegación:** la aplicación se compone de varias vistas independientes, cada una con su propia URL y propósito —Inicio (`/`), Productos (`/products`), Detalle de producto (`/products/:id`), Mis Favoritos (`/favorites`), Carrito (`/cart`) y Configuración (`/settings`)—. Un **menú lateral plegable** situado a la izquierda permite moverse entre ellas; un botón (icono ☰) lo muestra u oculta para ampliar el área de contenido. Al ser una SPA con enrutado del lado del cliente (React Router), el cambio de vista no recarga la página: solo se re-renderiza el área de contenido, ofreciendo una experiencia fluida.

Objetivos de calidad:

- **Arquitectura limpia y por capas**, con separación clara de responsabilidades (presentación, lógica de acceso a datos, estado de sesión).
- **Integración segura con el backend** mediante JWT.
- **Código mantenible y ampliable**: añadir nuevas funcionalidades no debe requerir reescribir las existentes.
- **Decisiones de diseño documentadas**, cada una con su justificación y las alternativas consideradas (ver sección 2).

Se priorizan soluciones sencillas, estándar y ampliamente utilizadas en proyectos profesionales frente a librerías avanzadas, buscando un código claro y fácil de mantener.

---

## 2. Decisiones técnicas (ADR resumido)

Cada elección se documenta con su motivo.

| Elección | Justificación |
|---|---|
| **Vite + React** | Arranque y recarga rápidos; estándar moderno del ecosistema React. |
| **JavaScript** | Menor fricción y foco en la lógica de la aplicación y la integración con la API. Migración a TypeScript prevista como evolución futura. |
| **Tailwind CSS** | Desarrollo rápido de UI, diseño totalmente bajo control del equipo, resultado consistente y responsive. |
| **React Router** | Enrutado estándar del lado del cliente para SPAs en React; habilita la navegación entre vistas sin recargar la página. |
| **axios con instancia única + interceptores** | Centraliza la configuración HTTP, la inyección del token JWT y el manejo global de errores en un único punto. |
| **useState / useEffect para el estado de datos** | Modelo nativo de React, sin dependencias adicionales; suficiente para el alcance actual. |
| **CORS en el backend** (en lugar de proxy de Vite) | Autoriza explícitamente el origen del frontend; es el mecanismo estándar, funciona en todos los entornos (dev, Docker, producción) y desacopla el cliente vía URL configurable. |

**Fuera de alcance en esta iteración (YAGNI):** TypeScript, gestores de estado global (Redux, Zustand), React Query, librerías de formularios/validación (React Hook Form, Zod), renderizado en servidor (Next.js) y pruebas automatizadas de frontend. Se contemplan como posibles evoluciones, no como parte de la entrega inicial.

---

## 3. Ubicación y conexión con el backend

- El frontend reside en **`smartcommerce/frontend/`**, dentro del mismo repositorio que el backend (**monorepo**), lo que facilita orquestar ambos con Docker.
- En desarrollo: frontend servido por Vite (`:5173`), backend en Spring Boot (`:8080`).
- **Conexión mediante CORS:** el frontend llama directamente al backend (`http://localhost:8080/api/v1`) y el backend autoriza explícitamente el origen del frontend configurando **CORS en Spring Security** (orígenes, métodos y cabeceras permitidos, incluida `Authorization` para el JWT).
  - La URL base del backend se lee de una variable de entorno (`VITE_API_URL`), de modo que sea configurable entre entornos (desarrollo, Docker, producción) sin tocar el código.
  - *Alternativa considerada:* proxy de desarrollo de Vite. Se descarta porque solo funciona en desarrollo; **CORS es el mecanismo estándar**, funciona igual en todos los entornos y es una configuración de seguridad relevante.

---

## 4. Estructura por capas

```
frontend/src/
├── api/            → axiosClient.js (instancia única de axios + interceptores)
├── services/       → authService.js, productService.js, cartService.js, userService.js, addressService.js
├── context/        → AuthContext.jsx (usuario + token, login/logout)
├── hooks/          → useAuth.js (acceso al AuthContext)
├── components/     → reutilizables: Button, Input, Sidebar, ProductCard, Spinner, EmptyState...
├── pages/          → Login.jsx, Home.jsx, Products.jsx, ProductDetail.jsx, Favorites.jsx, Cart.jsx, Settings.jsx...
├── layouts/        → MainLayout.jsx (menú lateral persistente + área de contenido)
├── routes/         → AppRoutes.jsx + ProtectedRoute.jsx
├── utils/          → utilidades puntuales (formateo de precio, etc.)
├── App.jsx
└── main.jsx
```

**Principio de acceso a datos:** ningún componente accede a axios directamente. El flujo es siempre **componente → service → axiosClient**. Esto concentra el conocimiento de la API en la capa de servicios y ofrece un punto único de cambio ante modificaciones del contrato.

---

## 5. Flujo de autenticación (JWT)

1. `Login.jsx` recoge las credenciales con `useState` e invoca `authService.login(credentials)`.
2. El backend responde con `{ token, expiresIn, userId, email, role }`.
3. El token y los datos de usuario se almacenan en `localStorage` y en el `AuthContext` (estado de sesión global).
4. El **interceptor de peticiones** de axios añade la cabecera `Authorization: Bearer <token>` automáticamente en cada llamada.
5. `ProtectedRoute` consulta el `AuthContext`: si no hay sesión activa, redirige a `/login`.
6. El **interceptor de respuestas** detecta un `401`, limpia la sesión (localStorage + contexto) y redirige al login.
7. El campo `role` queda disponible para diferenciar interfaces de administrador y usuario en fases posteriores.

---

## 6. Patrón de carga de datos

Cada vista que consume datos sigue el mismo modelo de tres estados con `useState` + `useEffect`:

```js
const [data, setData]       = useState(null);
const [loading, setLoading] = useState(true);
const [error, setError]     = useState(null);

useEffect(() => {
  productService.getAll()
    .then(res => setData(res))
    .catch(err => setError(err))
    .finally(() => setLoading(false));
}, []);
```

El renderizado responde al estado: carga → indicador de progreso; error → mensaje de error; conjunto vacío → pantalla de "sin resultados"; con datos → contenido. Este patrón garantiza una experiencia de usuario coherente en todas las vistas.

**Paginación:** el endpoint de productos devuelve una estructura `Page` de Spring (`{ content, totalPages, totalElements, number, size, ... }`). El `productService` y la vista de productos consumen `content` para el listado y los metadatos para la paginación.

---

## 7. Endpoints consumidos

| Servicio | Método + ruta | Notas |
|---|---|---|
| `authService.register` | `POST /api/v1/auth/register` | Devuelve `AuthResponse`. Público. |
| `authService.login` | `POST /api/v1/auth/login` | Devuelve `AuthResponse`. Público. |
| `productService.getAll` | `GET /api/v1/products` | Paginado (`Page`), admite filtros y `pageable`. Público (GET). |
| `productService.getById` | `GET /api/v1/products/{id}` | Público (GET). |
| `cartService.*` | `/api/v1/cart` (CRUD) | Requiere JWT. |
| `userService.getMyProfile` | `GET /api/v1/users/me` | Perfil del usuario. Requiere JWT. |
| `userService.updateMyProfile` | `PUT /api/v1/users/me` | Actualiza el perfil. Requiere JWT. |
| `addressService.*` | `/api/v1/addresses` (CRUD + `PATCH /{id}/default`) | Direcciones del usuario. Requiere JWT. |

Los endpoints de administración de producto y de otros módulos quedan fuera del alcance inicial y se incorporarán cuando corresponda.

---

## 8. Alcance funcional y orden de entrega

1. **Login** — incluye la base del proyecto: `axiosClient`, `AuthContext`, `AppRoutes`, `ProtectedRoute` y `MainLayout` con el menú lateral.
2. **Inicio** — vista de bienvenida tras iniciar sesión, punto de entrada de la navegación.
3. **Catálogo de productos** — listado paginado, `ProductCard` y estados de carga/error/vacío.
4. **Detalle de producto** — vista individual por identificador.
5. **Carrito** — visualización, alta, modificación de cantidad y eliminación de ítems.
6. **Configuración** — gestión del perfil del usuario (`/users/me`) y de sus direcciones (`/addresses`: alta, edición, borrado y marcar como predeterminada).
7. **Mis Favoritos** — vista *placeholder* ("próximamente") hasta que exista el módulo *wishlist* en el backend; entonces mostrará la lista de deseos del usuario.
8. **Registro** y vistas de módulos futuros del backend, a medida que se desarrollen.

Cada vista se construye de forma independiente y verificable antes de pasar a la siguiente.

**Nota sobre el menú lateral:** el menú lateral se ubica a la izquierda y es **plegable** mediante un botón (icono ☰) en la cabecera del área de contenido, que lo muestra u oculta.

---

## 9. Criterios de éxito

- El proyecto arranca con `npm run dev` y se comunica con el backend a través del proxy.
- Autenticación funcional: valida contra el backend, persiste el JWT y protege las rutas privadas.
- Catálogo de productos que carga datos reales del backend y gestiona los estados de carga, error y vacío.
- Código organizado por capas, con componentes pequeños y reutilizables y el flujo de acceso a datos componente → service → axiosClient.
- Interfaz limpia y responsive con Tailwind CSS.

---

## 10. Estabilidad frente a cambios del backend

El frontend depende del **contrato HTTP** (rutas y forma del JSON), no de la implementación interna del backend. En consecuencia:

- Refactorizar internamente el backend no afecta al frontend.
- Incorporar módulos nuevos (pedidos, pagos, lista de deseos...) solo **añade** vistas; las existentes siguen operativas.
- El único cambio que afectaría al frontend es **modificar el contrato de un endpoint ya existente** (renombrar un campo o una ruta), y se resuelve actualizando el service correspondiente, que actúa como punto único de cambio.
