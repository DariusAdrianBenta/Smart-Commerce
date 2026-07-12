# SmartCommerce Frontend – Plan de Implementación

> **Para quien ejecuta:** este plan se construye tarea a tarea. Cada tarea termina con una verificación manual (arrancar la app y observar el comportamiento) y un commit. Los pasos usan casillas (`- [ ]`) para seguimiento.

**Objetivo:** construir una SPA en React que consuma la API REST de SmartCommerce: autenticación con JWT, catálogo de productos, detalle y carrito.

**Arquitectura:** SPA por capas. Los componentes consumen *services*, los services usan una única instancia de axios con interceptores para el JWT y los errores. El estado de sesión vive en un `AuthContext`. Los datos de cada vista se cargan con `useState` + `useEffect` (patrón de tres estados: carga/error/datos).

**Tech Stack:** Vite, React (JavaScript), React Router, axios, Tailwind CSS.

## Restricciones globales (aplican a todas las tareas)

- **Solo JavaScript.** Nada de TypeScript.
- **Sin librerías avanzadas:** nada de Redux, Zustand, React Query, React Hook Form ni Zod. Estado con hooks nativos.
- **Estilos solo con Tailwind CSS.**
- **Regla de acceso a datos:** un componente NUNCA importa axios directamente. Flujo obligatorio: **componente → service → `axiosClient`**.
- **Carga de datos:** `useState` + `useEffect` con los tres estados (loading / error / datos).
- **Conexión con el back:** el front llama a rutas relativas `/api/...`; el proxy de Vite las redirige a `http://localhost:8080`.
- **Backend:** debe estar arrancado en `:8080` con PostgreSQL levantado (`docker-compose up -d` en la carpeta `smartcommerce`) para las verificaciones que tocan la API. Usuario admin sembrado: `admin@smartcommerce.com` / `Admin1234!`.
- **Ubicación:** todo el frontend vive en `C:\Users\dariu\Desktop\SmartCommerce\frontend\`.

---

## Tarea 1: Base del proyecto (scaffold)

**Deliverable:** una app React en blanco que arranca con `npm run dev`, con Tailwind funcionando, la estructura de carpetas creada, el proxy configurado y git inicializado.

**Files:**
- Create: `frontend/` (proyecto Vite completo)
- Create: `frontend/vite.config.js` (proxy `/api` → `:8080`)
- Create: `frontend/tailwind.config.js`, `frontend/postcss.config.js`
- Modify: `frontend/src/index.css` (directivas de Tailwind)
- Create: estructura de carpetas en `frontend/src/` (`api`, `services`, `context`, `hooks`, `components`, `pages`, `layouts`, `routes`, `utils`)

- [ ] **Paso 1: Crear el proyecto Vite**

```bash
cd /c/Users/dariu/Desktop/SmartCommerce
npm create vite@latest frontend -- --template react
cd frontend
npm install
```

- [ ] **Paso 2: Instalar dependencias del stack**

```bash
npm install react-router-dom axios
npm install -D tailwindcss@3 postcss autoprefixer
npx tailwindcss init -p
```

*(Se fija Tailwind v3 por estabilidad; v4 cambió la configuración.)*

- [ ] **Paso 3: Configurar Tailwind** — `frontend/tailwind.config.js`:

```js
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: { extend: {} },
  plugins: [],
};
```

- [ ] **Paso 4: Directivas de Tailwind** — reemplazar el contenido de `frontend/src/index.css` por:

```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

- [ ] **Paso 5: Configurar el proxy** — `frontend/vite.config.js`:

```js
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
```

- [ ] **Paso 6: Crear la estructura de carpetas**

```bash
cd /c/Users/dariu/Desktop/SmartCommerce/frontend/src
mkdir api services context hooks components pages layouts routes utils
```

- [ ] **Paso 7: Dejar `App.jsx` mínimo** con una clase Tailwind visible para verificar que funciona — `frontend/src/App.jsx`:

```jsx
export default function App() {
  return (
    <h1 className="text-3xl font-bold text-blue-600 p-8">
      SmartCommerce funcionando
    </h1>
  );
}
```

- [ ] **Paso 8: Arrancar y verificar**

Run: `npm run dev`
Esperado: en `http://localhost:5173` se ve "SmartCommerce funcionando" en azul, grande y negrita (confirma que Tailwind aplica estilos).

- [ ] **Paso 9: Inicializar git y commit**

```bash
cd /c/Users/dariu/Desktop/SmartCommerce/frontend
git init
git add -A
git commit -m "chore: scaffold React+Vite+Tailwind con proxy y estructura por capas"
```

*(Vite ya genera un `.gitignore` que excluye `node_modules`.)*

---

## Tarea 2: Autenticación y Login

**Deliverable:** un usuario puede iniciar sesión contra el backend; el JWT se guarda y se adjunta a las peticiones; las rutas privadas quedan protegidas.

**Files:**
- Create: `frontend/src/api/axiosClient.js`
- Create: `frontend/src/services/authService.js`
- Create: `frontend/src/context/AuthContext.jsx`
- Create: `frontend/src/hooks/useAuth.js`
- Create: `frontend/src/routes/ProtectedRoute.jsx`
- Create: `frontend/src/routes/AppRoutes.jsx`
- Create: `frontend/src/components/Sidebar.jsx` (menú lateral persistente)
- Create: `frontend/src/layouts/MainLayout.jsx`
- Create: `frontend/src/pages/Login.jsx`
- Create: `frontend/src/pages/Home.jsx` (vista de Inicio)
- Create: `frontend/src/pages/Favorites.jsx` (placeholder de Mis Favoritos)
- Create: `frontend/src/components/Button.jsx`, `frontend/src/components/Input.jsx`
- Modify: `frontend/src/App.jsx`, `frontend/src/main.jsx`

**Interfaces que produce (las usan tareas posteriores):**
- `axiosClient` — instancia de axios preconfigurada (default export).
- `useAuth()` → `{ user, token, login(credentials), logout(), isAuthenticated }`.
- `authService.login({ email, password })` → `Promise<AuthResponse>` con `{ token, expiresIn, userId, email, role }`.

- [ ] **Paso 1: `axiosClient.js`** — instancia única + interceptores:

```js
import axios from "axios";

const axiosClient = axios.create({
  baseURL: "/api/v1",
  headers: { "Content-Type": "application/json" },
});

// Interceptor de petición: adjunta el JWT si existe.
axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Interceptor de respuesta: si 401, limpia sesión y manda al login.
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
```

- [ ] **Paso 2: `authService.js`**:

```js
import axiosClient from "../api/axiosClient";

export const authService = {
  login: (credentials) =>
    axiosClient.post("/auth/login", credentials).then((res) => res.data),
  register: (data) =>
    axiosClient.post("/auth/register", data).then((res) => res.data),
};
```

- [ ] **Paso 3: `AuthContext.jsx`** — estado de sesión global:

```jsx
import { createContext, useState } from "react";
import { authService } from "../services/authService";

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem("token"));
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem("user");
    return raw ? JSON.parse(raw) : null;
  });

  const login = async (credentials) => {
    const data = await authService.login(credentials);
    const userData = { id: data.userId, email: data.email, role: data.role };
    localStorage.setItem("token", data.token);
    localStorage.setItem("user", JSON.stringify(userData));
    setToken(data.token);
    setUser(userData);
    return data;
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{ user, token, login, logout, isAuthenticated: !!token }}
    >
      {children}
    </AuthContext.Provider>
  );
}
```

- [ ] **Paso 4: `useAuth.js`**:

```js
import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";

export function useAuth() {
  return useContext(AuthContext);
}
```

- [ ] **Paso 5: `ProtectedRoute.jsx`**:

```jsx
import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

export default function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to="/login" replace />;
}
```

- [ ] **Paso 6: Componentes `Button.jsx` e `Input.jsx`** (reutilizables, presentacionales):

```jsx
// Button.jsx
export default function Button({ children, ...props }) {
  return (
    <button
      className="w-full rounded-md bg-blue-600 py-2 px-4 text-white font-medium hover:bg-blue-700 disabled:opacity-50"
      {...props}
    >
      {children}
    </button>
  );
}
```

```jsx
// Input.jsx
export default function Input({ label, ...props }) {
  return (
    <label className="block mb-4">
      <span className="block text-sm font-medium text-gray-700 mb-1">{label}</span>
      <input
        className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none"
        {...props}
      />
    </label>
  );
}
```

- [ ] **Paso 7: `Login.jsx`** — formulario con `useState`, estados de carga/error:

```jsx
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import Input from "../components/Input";
import Button from "../components/Button";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login({ email, password });
      navigate("/");
    } catch {
      setError("Credenciales incorrectas");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <form onSubmit={handleSubmit} className="bg-white p-8 rounded-lg shadow-md w-full max-w-sm">
        <h1 className="text-2xl font-bold mb-6 text-center">Iniciar sesión</h1>
        {error && <p className="mb-4 text-sm text-red-600">{error}</p>}
        <Input label="Email" type="email" value={email}
          onChange={(e) => setEmail(e.target.value)} required />
        <Input label="Contraseña" type="password" value={password}
          onChange={(e) => setPassword(e.target.value)} required />
        <Button type="submit" disabled={loading}>
          {loading ? "Entrando..." : "Entrar"}
        </Button>
      </form>
    </div>
  );
}
```

- [ ] **Paso 8: Páginas `Home.jsx` (Inicio) y `Favorites.jsx` (placeholder)**

```jsx
// Home.jsx
import { useAuth } from "../hooks/useAuth";

export default function Home() {
  const { user } = useAuth();
  return (
    <div className="p-8">
      <h1 className="text-3xl font-bold mb-2">Bienvenido a SmartCommerce</h1>
      <p className="text-gray-600">Sesión iniciada como <strong>{user?.email}</strong>.</p>
    </div>
  );
}
```

```jsx
// Favorites.jsx  (Mis Favoritos: placeholder hasta que exista el módulo wishlist en el backend)
export default function Favorites() {
  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-2">Mis Favoritos</h1>
      <p className="text-gray-500">
        Próximamente: esta sección mostrará tu lista de deseos cuando el módulo
        de wishlist esté disponible en el backend.
      </p>
    </div>
  );
}
```

- [ ] **Paso 9: `Sidebar.jsx`** — menú lateral a la izquierda (`border-r`). Usa `NavLink` para resaltar la vista activa. El estado de mostrar/ocultar lo gestiona `MainLayout` (Paso 10):

```jsx
import { NavLink } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

const links = [
  { to: "/", label: "Inicio", end: true },
  { to: "/products", label: "Productos" },
  { to: "/favorites", label: "Mis Favoritos" },
  { to: "/cart", label: "Carrito" },
  { to: "/settings", label: "Configuración" },
];

export default function Sidebar() {
  const { user, logout } = useAuth();
  return (
    <aside className="w-60 shrink-0 bg-white border-r min-h-screen flex flex-col p-6">
      <h2 className="text-xl font-bold text-blue-600 mb-8">SmartCommerce</h2>
      <nav className="flex flex-col gap-2">
        {links.map((l) => (
          <NavLink key={l.to} to={l.to} end={l.end}
            className={({ isActive }) =>
              `rounded px-3 py-2 ${isActive
                ? "bg-blue-50 text-blue-700 font-medium"
                : "text-gray-700 hover:bg-gray-100"}`
            }>
            {l.label}
          </NavLink>
        ))}
      </nav>
      <div className="mt-auto pt-6 border-t">
        <p className="text-sm text-gray-500 mb-2">{user?.email}</p>
        <button onClick={logout} className="text-sm text-red-600 hover:underline">
          Cerrar sesión
        </button>
      </div>
    </aside>
  );
}
```

- [ ] **Paso 10: `MainLayout.jsx`** — menú lateral plegable (a la izquierda) + área de contenido con cabecera y botón ☰. El estado `open` se gestiona aquí con `useState`; el contenido de cada vista sale por el `Outlet`:

```jsx
import { useState } from "react";
import { Outlet } from "react-router-dom";
import Sidebar from "../components/Sidebar";

export default function MainLayout() {
  const [open, setOpen] = useState(true);
  return (
    <div className="min-h-screen flex bg-gray-50">
      {open && <Sidebar />}
      <div className="flex-1">
        <header className="bg-white border-b px-4 py-3">
          <button
            onClick={() => setOpen((prev) => !prev)}
            aria-label="Mostrar u ocultar el menú"
            className="text-2xl leading-none px-2 hover:text-blue-600"
          >
            ☰
          </button>
        </header>
        <main><Outlet /></main>
      </div>
    </div>
  );
}
```

*(El icono ☰ es un carácter Unicode, así que no hace falta ninguna librería de iconos. Se puede sustituir por un SVG más adelante.)*

- [ ] **Paso 11: `AppRoutes.jsx`** — mapa de rutas:

```jsx
import { Routes, Route } from "react-router-dom";
import Login from "../pages/Login";
import Home from "../pages/Home";
import Favorites from "../pages/Favorites";
import MainLayout from "../layouts/MainLayout";
import ProtectedRoute from "./ProtectedRoute";

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<Home />} />
        <Route path="/favorites" element={<Favorites />} />
        {/* Productos, detalle y carrito se añaden en tareas siguientes */}
      </Route>
    </Routes>
  );
}
```

- [ ] **Paso 12: Conectar `main.jsx` y `App.jsx`**

```jsx
// App.jsx
import AppRoutes from "./routes/AppRoutes";
export default function App() {
  return <AppRoutes />;
}
```

```jsx
// main.jsx
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import App from "./App.jsx";
import "./index.css";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>
);
```

- [ ] **Paso 13: Verificar el flujo completo** (backend + PostgreSQL arrancados)

1. Ir a `http://localhost:5173/` → debe redirigir a `/login` (ruta protegida sin sesión).
2. Iniciar sesión con `admin@smartcommerce.com` / `Admin1234!` → redirige a `/` y muestra el email y rol.
3. Abrir DevTools → Application → Local Storage: existen `token` y `user`.
4. En Network, una petición posterior lleva la cabecera `Authorization: Bearer ...`.
5. El botón ☰ de la cabecera oculta y vuelve a mostrar el menú lateral (izquierda).
6. "Cerrar sesión" (en el menú lateral) → vuelve a `/login` y desaparecen `token`/`user`.

- [ ] **Paso 14: Commit**

```bash
git add -A
git commit -m "feat: autenticación con JWT (AuthContext, axios interceptors, login, rutas protegidas)"
```

---

## Tarea 3: Catálogo de productos

**Deliverable:** una vista que lista los productos reales del backend, paginada, con estados de carga/error/vacío.

**Files:**
- Create: `frontend/src/services/productService.js`
- Create: `frontend/src/pages/Products.jsx`
- Create: `frontend/src/components/ProductCard.jsx`
- Create: `frontend/src/components/Spinner.jsx`
- Create: `frontend/src/components/EmptyState.jsx`
- Create: `frontend/src/utils/formatPrice.js`
- Modify: `frontend/src/routes/AppRoutes.jsx` (añadir ruta `/products`)

**Interfaces que produce:**
- `productService.getAll({ page, size })` → `Promise<Page>` con `{ content: Product[], totalPages, totalElements, number, size }`.
- `Product` = `{ id, name, description, price, stock, status, brand, categoryId, categoryName, imageUrls }`.

- [ ] **Paso 1: `productService.js`**:

```js
import axiosClient from "../api/axiosClient";

export const productService = {
  getAll: (params = {}) =>
    axiosClient.get("/products", { params }).then((res) => res.data),
  getById: (id) =>
    axiosClient.get(`/products/${id}`).then((res) => res.data),
};
```

- [ ] **Paso 2: `formatPrice.js`**:

```js
export function formatPrice(value) {
  return new Intl.NumberFormat("es-ES", {
    style: "currency",
    currency: "EUR",
  }).format(value);
}
```

- [ ] **Paso 3: `Spinner.jsx` y `EmptyState.jsx`**:

```jsx
// Spinner.jsx
export default function Spinner() {
  return (
    <div className="flex justify-center py-16">
      <div className="h-10 w-10 animate-spin rounded-full border-4 border-gray-300 border-t-blue-600" />
    </div>
  );
}
```

```jsx
// EmptyState.jsx
export default function EmptyState({ message = "No hay resultados" }) {
  return <p className="text-center text-gray-500 py-16">{message}</p>;
}
```

- [ ] **Paso 4: `ProductCard.jsx`**:

```jsx
import { Link } from "react-router-dom";
import { formatPrice } from "../utils/formatPrice";

export default function ProductCard({ product }) {
  return (
    <Link to={`/products/${product.id}`}
      className="block rounded-lg border bg-white p-4 shadow-sm hover:shadow-md transition">
      <div className="aspect-square mb-3 bg-gray-100 rounded overflow-hidden">
        {product.imageUrls?.[0] && (
          <img src={product.imageUrls[0]} alt={product.name}
            className="h-full w-full object-cover" />
        )}
      </div>
      <h3 className="font-medium truncate">{product.name}</h3>
      <p className="text-sm text-gray-500">{product.brand}</p>
      <p className="mt-2 font-bold text-blue-600">{formatPrice(product.price)}</p>
    </Link>
  );
}
```

- [ ] **Paso 5: `Products.jsx`** — patrón de tres estados + paginación:

```jsx
import { useState, useEffect } from "react";
import { productService } from "../services/productService";
import ProductCard from "../components/ProductCard";
import Spinner from "../components/Spinner";
import EmptyState from "../components/EmptyState";

export default function Products() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    productService.getAll({ page, size: 12 })
      .then((res) => setData(res))
      .catch(() => setError("No se pudieron cargar los productos"))
      .finally(() => setLoading(false));
  }, [page]);

  if (loading) return <Spinner />;
  if (error) return <p className="text-center text-red-600 py-16">{error}</p>;
  if (!data || data.content.length === 0) return <EmptyState message="No hay productos" />;

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-6">Productos</h1>
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {data.content.map((p) => <ProductCard key={p.id} product={p} />)}
      </div>
      <div className="flex justify-center gap-4 mt-8">
        <button disabled={data.number === 0} onClick={() => setPage(page - 1)}
          className="px-4 py-2 rounded border disabled:opacity-40">Anterior</button>
        <span className="py-2">Página {data.number + 1} de {data.totalPages}</span>
        <button disabled={data.number + 1 >= data.totalPages} onClick={() => setPage(page + 1)}
          className="px-4 py-2 rounded border disabled:opacity-40">Siguiente</button>
      </div>
    </div>
  );
}
```

- [ ] **Paso 6: Añadir la ruta `/products`** en `AppRoutes.jsx` dentro del bloque protegido:

```jsx
import Products from "../pages/Products";
// ...
<Route path="/products" element={<Products />} />
```

- [ ] **Paso 7: Verificar**

1. Con sesión iniciada, ir a `/products`.
2. Si hay productos sembrados, se ven las tarjetas; si no, aparece "No hay productos".
3. Apagar el backend y recargar → aparece el mensaje de error (verifica el estado de error).
4. La paginación habilita/deshabilita "Anterior"/"Siguiente" según la página.

- [ ] **Paso 8: Commit**

```bash
git add -A
git commit -m "feat: catálogo de productos paginado con estados de carga/error/vacío"
```

---

## Tarea 4: Detalle de producto

**Deliverable:** vista de un producto individual por su id.

**Files:**
- Create: `frontend/src/pages/ProductDetail.jsx`
- Modify: `frontend/src/routes/AppRoutes.jsx` (ruta `/products/:id`)

- [ ] **Paso 1: `ProductDetail.jsx`** — usa `useParams` + patrón de tres estados:

```jsx
import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { productService } from "../services/productService";
import { formatPrice } from "../utils/formatPrice";
import Spinner from "../components/Spinner";

export default function ProductDetail() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setLoading(true);
    productService.getById(id)
      .then((res) => setProduct(res))
      .catch(() => setError("Producto no encontrado"))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Spinner />;
  if (error) return <p className="text-center text-red-600 py-16">{error}</p>;

  return (
    <div className="p-8 max-w-4xl mx-auto grid md:grid-cols-2 gap-8">
      <div className="aspect-square bg-gray-100 rounded overflow-hidden">
        {product.imageUrls?.[0] && (
          <img src={product.imageUrls[0]} alt={product.name}
            className="h-full w-full object-cover" />
        )}
      </div>
      <div>
        <h1 className="text-3xl font-bold mb-2">{product.name}</h1>
        <p className="text-gray-500 mb-4">{product.brand}</p>
        <p className="text-2xl font-bold text-blue-600 mb-4">{formatPrice(product.price)}</p>
        <p className="text-gray-700 mb-4">{product.description}</p>
        <p className="text-sm text-gray-500">Stock: {product.stock}</p>
      </div>
    </div>
  );
}
```

- [ ] **Paso 2: Añadir la ruta** en `AppRoutes.jsx` (bloque protegido):

```jsx
import ProductDetail from "../pages/ProductDetail";
// ...
<Route path="/products/:id" element={<ProductDetail />} />
```

- [ ] **Paso 3: Verificar** — hacer clic en una `ProductCard` navega a `/products/:id` y muestra el detalle. Un id inexistente muestra "Producto no encontrado".

- [ ] **Paso 4: Commit**

```bash
git add -A
git commit -m "feat: vista de detalle de producto"
```

---

## Tarea 5: Carrito

**Deliverable:** el usuario puede ver su carrito, añadir productos, cambiar cantidades y eliminar ítems.

**Endpoints (requieren JWT):**
- `GET /api/v1/cart` → `CartResponseDTO`
- `POST /api/v1/cart/items` body `{ productId, quantity }` → `CartResponseDTO`
- `PUT /api/v1/cart/items/{productId}` body `{ quantity }` → `CartResponseDTO`
- `DELETE /api/v1/cart/items/{productId}` → `CartResponseDTO`
- `DELETE /api/v1/cart` → 204

**Files:**
- Create: `frontend/src/services/cartService.js`
- Create: `frontend/src/pages/Cart.jsx`
- Modify: `frontend/src/pages/ProductDetail.jsx` (botón "Añadir al carrito")
- Modify: `frontend/src/routes/AppRoutes.jsx` (ruta `/cart`)

**Interfaz que produce:**
- `cartService` → `{ getMyCart(), addItem(productId, quantity), updateItem(productId, quantity), removeItem(productId), clear() }`.
- Forma real de la respuesta (verificada en el backend):
  - `CartResponseDTO` = `{ id, items: CartItem[], total }`
  - `CartItem` = `{ productId, productName, productPrice, productImage, quantity, subtotal }`

- [ ] **Paso 1: `cartService.js`**:

```js
import axiosClient from "../api/axiosClient";

export const cartService = {
  getMyCart: () => axiosClient.get("/cart").then((res) => res.data),
  addItem: (productId, quantity) =>
    axiosClient.post("/cart/items", { productId, quantity }).then((res) => res.data),
  updateItem: (productId, quantity) =>
    axiosClient.put(`/cart/items/${productId}`, { quantity }).then((res) => res.data),
  removeItem: (productId) =>
    axiosClient.delete(`/cart/items/${productId}`).then((res) => res.data),
  clear: () => axiosClient.delete("/cart").then((res) => res.data),
};
```

- [ ] **Paso 2: `Cart.jsx`** — patrón de tres estados; carga el carrito y permite eliminar ítems:

```jsx
import { useState, useEffect } from "react";
import { cartService } from "../services/cartService";
import { formatPrice } from "../utils/formatPrice";
import Spinner from "../components/Spinner";
import EmptyState from "../components/EmptyState";

export default function Cart() {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    cartService.getMyCart()
      .then((res) => setCart(res))
      .catch(() => setError("No se pudo cargar el carrito"))
      .finally(() => setLoading(false));
  }, []);

  const handleRemove = async (productId) => {
    const updated = await cartService.removeItem(productId);
    setCart(updated);
  };

  if (loading) return <Spinner />;
  if (error) return <p className="text-center text-red-600 py-16">{error}</p>;
  if (!cart || cart.items.length === 0) return <EmptyState message="Tu carrito está vacío" />;

  return (
    <div className="p-8 max-w-3xl mx-auto">
      <h1 className="text-2xl font-bold mb-6">Carrito</h1>
      <ul className="divide-y">
        {cart.items.map((item) => (
          <li key={item.productId} className="flex justify-between items-center py-4">
            <div>
              <p className="font-medium">{item.productName}</p>
              <p className="text-sm text-gray-500">
                {item.quantity} × {formatPrice(item.productPrice)} = {formatPrice(item.subtotal)}
              </p>
            </div>
            <button onClick={() => handleRemove(item.productId)}
              className="text-red-600 hover:underline">Eliminar</button>
          </li>
        ))}
      </ul>
      <p className="text-right font-bold text-xl mt-6">
        Total: {formatPrice(cart.total)}
      </p>
    </div>
  );
}
```

- [ ] **Paso 3: Botón "Añadir al carrito"** en `ProductDetail.jsx` — importar `cartService`, `useState` para feedback, y un `Button` que llame a `cartService.addItem(product.id, 1)` y muestre "Añadido".

- [ ] **Paso 4: Añadir la ruta `/cart`** en `AppRoutes.jsx` (bloque protegido):

```jsx
import Cart from "../pages/Cart";
// ...
<Route path="/cart" element={<Cart />} />
```

- [ ] **Paso 5: Verificar** (con sesión iniciada)

1. En el detalle de un producto, pulsar "Añadir al carrito".
2. Ir a `/cart`: aparece el producto con su cantidad y precio.
3. "Eliminar" lo quita y actualiza el total.
4. Con el carrito vacío, aparece "Tu carrito está vacío".

- [ ] **Paso 6: Commit**

```bash
git add -A
git commit -m "feat: carrito (ver, añadir, eliminar) integrado con la API"
```

---

## Tarea 6: Configuración (perfil + direcciones)

**Deliverable:** una vista donde el usuario ve y edita su perfil y gestiona sus direcciones (alta, borrado y marcar como predeterminada).

**Endpoints (requieren JWT):**
- `GET /api/v1/users/me` → `UserResponseDTO`
- `PUT /api/v1/users/me` body `{ firstName, lastName, phone, birthDate }` → `UserResponseDTO`
- `GET /api/v1/addresses` → `AddressResponseDTO[]`
- `POST /api/v1/addresses` body `AddressRequest` → `AddressResponseDTO`
- `DELETE /api/v1/addresses/{id}` → 204
- `PATCH /api/v1/addresses/{id}/default` → 204

**Formas de datos (verificadas en el backend):**
- `UserResponseDTO` = `{ id, firstName, lastName, phone, birthDate, email, role, active, createdAt }`
- Editable del perfil = `{ firstName, lastName, phone, birthDate }` (email no editable)
- `AddressResponseDTO` = `{ id, street, addressLine2, city, province, postalCode, country, isDefault }`
- `AddressRequest` = `{ street, addressLine2?, city, province, postalCode, country }` (obligatorios todos salvo `addressLine2`)

**Files:**
- Create: `frontend/src/services/userService.js`
- Create: `frontend/src/services/addressService.js`
- Create: `frontend/src/pages/Settings.jsx`
- Modify: `frontend/src/routes/AppRoutes.jsx` (ruta `/settings`)

- [ ] **Paso 1: `userService.js`**:

```js
import axiosClient from "../api/axiosClient";

export const userService = {
  getMyProfile: () => axiosClient.get("/users/me").then((r) => r.data),
  updateMyProfile: (data) => axiosClient.put("/users/me", data).then((r) => r.data),
};
```

- [ ] **Paso 2: `addressService.js`**:

```js
import axiosClient from "../api/axiosClient";

export const addressService = {
  getMyAddresses: () => axiosClient.get("/addresses").then((r) => r.data),
  add: (data) => axiosClient.post("/addresses", data).then((r) => r.data),
  update: (id, data) => axiosClient.put(`/addresses/${id}`, data).then((r) => r.data),
  remove: (id) => axiosClient.delete(`/addresses/${id}`),
  setDefault: (id) => axiosClient.patch(`/addresses/${id}/default`),
};
```

- [ ] **Paso 3: `Settings.jsx`** — carga perfil y direcciones (dos peticiones en paralelo con `Promise.all`), formulario de perfil y gestión de direcciones:

```jsx
import { useState, useEffect } from "react";
import { userService } from "../services/userService";
import { addressService } from "../services/addressService";
import Input from "../components/Input";
import Button from "../components/Button";
import Spinner from "../components/Spinner";

const emptyAddress = {
  street: "", addressLine2: "", city: "", province: "", postalCode: "", country: "",
};

export default function Settings() {
  const [profile, setProfile] = useState(null);
  const [addresses, setAddresses] = useState([]);
  const [newAddress, setNewAddress] = useState(emptyAddress);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [savedMsg, setSavedMsg] = useState("");

  useEffect(() => {
    Promise.all([userService.getMyProfile(), addressService.getMyAddresses()])
      .then(([p, a]) => { setProfile(p); setAddresses(a); })
      .catch(() => setError("No se pudo cargar la configuración"))
      .finally(() => setLoading(false));
  }, []);

  const onProfileChange = (field) => (e) =>
    setProfile({ ...profile, [field]: e.target.value });

  const saveProfile = async (e) => {
    e.preventDefault();
    const { firstName, lastName, phone, birthDate } = profile;
    const updated = await userService.updateMyProfile({ firstName, lastName, phone, birthDate });
    setProfile(updated);
    setSavedMsg("Perfil actualizado");
    setTimeout(() => setSavedMsg(""), 2000);
  };

  const onNewAddressChange = (field) => (e) =>
    setNewAddress({ ...newAddress, [field]: e.target.value });

  const addAddress = async (e) => {
    e.preventDefault();
    const created = await addressService.add(newAddress);
    setAddresses([...addresses, created]);
    setNewAddress(emptyAddress);
  };

  const removeAddress = async (id) => {
    await addressService.remove(id);
    setAddresses(addresses.filter((a) => a.id !== id));
  };

  const makeDefault = async (id) => {
    await addressService.setDefault(id);
    setAddresses(addresses.map((a) => ({ ...a, isDefault: a.id === id })));
  };

  if (loading) return <Spinner />;
  if (error) return <p className="text-center text-red-600 py-16">{error}</p>;

  return (
    <div className="p-8 max-w-2xl mx-auto space-y-10">
      <section>
        <h1 className="text-2xl font-bold mb-4">Mi perfil</h1>
        <form onSubmit={saveProfile} className="bg-white p-6 rounded-lg shadow-sm">
          <Input label="Nombre" value={profile.firstName || ""} onChange={onProfileChange("firstName")} />
          <Input label="Apellidos" value={profile.lastName || ""} onChange={onProfileChange("lastName")} />
          <Input label="Teléfono" value={profile.phone || ""} onChange={onProfileChange("phone")} />
          <Input label="Fecha de nacimiento" type="date" value={profile.birthDate || ""} onChange={onProfileChange("birthDate")} />
          <p className="text-sm text-gray-500 mb-4">Email: {profile.email} (no editable)</p>
          <Button type="submit">Guardar cambios</Button>
          {savedMsg && <p className="text-green-600 text-sm mt-2">{savedMsg}</p>}
        </form>
      </section>

      <section>
        <h2 className="text-2xl font-bold mb-4">Mis direcciones</h2>
        {addresses.length === 0 && <p className="text-gray-500 mb-4">Aún no tienes direcciones.</p>}
        <ul className="space-y-3 mb-6">
          {addresses.map((a) => (
            <li key={a.id} className="bg-white p-4 rounded-lg shadow-sm flex justify-between items-start">
              <div>
                <p className="font-medium">
                  {a.street}{a.addressLine2 ? `, ${a.addressLine2}` : ""}
                  {a.isDefault && (
                    <span className="ml-2 text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded">Predeterminada</span>
                  )}
                </p>
                <p className="text-sm text-gray-500">{a.postalCode} {a.city}, {a.province} ({a.country})</p>
              </div>
              <div className="flex gap-3 text-sm">
                {!a.isDefault && (
                  <button onClick={() => makeDefault(a.id)} className="text-blue-600 hover:underline">
                    Hacer predeterminada
                  </button>
                )}
                <button onClick={() => removeAddress(a.id)} className="text-red-600 hover:underline">
                  Eliminar
                </button>
              </div>
            </li>
          ))}
        </ul>

        <form onSubmit={addAddress} className="bg-white p-6 rounded-lg shadow-sm">
          <h3 className="font-semibold mb-3">Añadir dirección</h3>
          <Input label="Calle" value={newAddress.street} onChange={onNewAddressChange("street")} required />
          <Input label="Piso / Puerta (opcional)" value={newAddress.addressLine2} onChange={onNewAddressChange("addressLine2")} />
          <Input label="Ciudad" value={newAddress.city} onChange={onNewAddressChange("city")} required />
          <Input label="Provincia" value={newAddress.province} onChange={onNewAddressChange("province")} required />
          <Input label="Código postal" value={newAddress.postalCode} onChange={onNewAddressChange("postalCode")} required />
          <Input label="País" value={newAddress.country} onChange={onNewAddressChange("country")} required />
          <Button type="submit">Añadir</Button>
        </form>
      </section>
    </div>
  );
}
```

- [ ] **Paso 4: Añadir la ruta `/settings`** en `AppRoutes.jsx` (bloque protegido):

```jsx
import Settings from "../pages/Settings";
// ...
<Route path="/settings" element={<Settings />} />
```

- [ ] **Paso 5: Verificar** (con sesión iniciada)

1. Ir a `/settings`: se cargan los datos del perfil.
2. Editar un campo del perfil y "Guardar cambios" → aparece "Perfil actualizado"; al recargar, el cambio persiste.
3. Añadir una dirección → aparece en la lista.
4. "Hacer predeterminada" → la etiqueta "Predeterminada" se mueve a esa dirección.
5. "Eliminar" → la dirección desaparece de la lista.

- [ ] **Paso 6: Commit**

```bash
git add -A
git commit -m "feat: página de configuración (perfil y direcciones)"
```

---

## Verificación final

- [ ] Flujo completo end-to-end: login → catálogo → detalle → añadir al carrito → ver carrito → configuración → logout.
- [ ] La UI es responsive (probar en móvil con las DevTools).
- [ ] Ningún componente importa `axios` directamente (todo pasa por un service).
- [ ] Los estados de carga/error/vacío funcionan en las tres vistas de datos.
