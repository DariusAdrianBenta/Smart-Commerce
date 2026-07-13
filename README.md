# SmartCommerce

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![React](https://img.shields.io/badge/React-Vite-61DAFB)
![Tailwind CSS](https://img.shields.io/badge/Tailwind-CSS-38BDF8)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)

Aplicación de e-commerce de tipo Amazon con un **backend en Spring Boot** y un
**frontend en React**, desarrollada como proyecto de portfolio para demostrar
arquitectura limpia, reglas de negocio reales y buenas prácticas de desarrollo.

---

## 📑 Índice

- [Objetivo](#-objetivo)
- [Metodología y desarrollo asistido por IA](#-metodología-y-desarrollo-asistido-por-ia)
- [Arquitectura y decisiones de diseño](#-arquitectura-y-decisiones-de-diseño)
- [Tecnologías](#-tecnologías)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Base de datos](#-base-de-datos)
- [Configuración](#-configuración)
- [Puesta en marcha y compilación](#-puesta-en-marcha-y-compilación)
- [Funcionalidades](#-funcionalidades)
- [Documentación de la API](#-documentación-de-la-api)
- [Docker](#-docker)
- [Autor](#-autor)

---

## 📌 Objetivo

**SmartCommerce** es un proyecto personal con un propósito doble: por un lado,
construir un sistema de e-commerce profesional y escalable; por otro, servir
como vehículo de aprendizaje para **profundizar y especializarme en Spring
Boot**.

El proyecto se aborda de forma deliberada como un entorno de trabajo realista:
en lugar de evitar los problemas técnicos que van surgiendo, me enfrento a ellos
y los resuelvo con la mejor solución posible en cada caso. De este modo simulo
los conflictos y las decisiones que aparecerían en un contexto laboral real,
demostrando capacidad de **análisis y de aportar soluciones sólidas** a
problemas concretos.

A nivel técnico, el proyecto aplica:

- Arquitectura limpia y modular (*modular monolith*).
- Reglas de negocio reales (stock, carrito, direcciones, roles…).
- Seguridad basada en JWT.
- Separación estricta de capas y uso obligatorio de DTOs.
- Un frontend desacoplado que consume la API REST.

A nivel de despliegue, el objetivo final es que toda la aplicación (base de
datos, backend y frontend) se pueda ejecutar con **un único comando** mediante
Docker, sin instalaciones previas.

---

## 🤖 Metodología y desarrollo asistido por IA

El proyecto se desarrolla con el apoyo de herramientas de inteligencia
artificial (**Claude Code** y **ChatGPT**), integradas en el flujo de trabajo
para ganar productividad sin renunciar al control ni a la comprensión del
código.

El planteamiento es claro: **yo dirijo, la IA ejecuta.** Actúo como responsable
técnico del proyecto —defino la arquitectura, tomo las decisiones de diseño,
establezco las pautas y reviso cada resultado—, mientras que la IA acelera la
implementación dentro de esas directrices. Cada pieza de código se entiende, se
valida y se justifica: nada se incorpora "a ciegas".

- **Claude Code** actúa como asistente de implementación: generación de código
  guiada, refactorizaciones y automatización de tareas repetitivas, siempre
  siguiendo las especificaciones y los planes que defino previamente.
- **ChatGPT** actúa como apoyo en la toma de decisiones y como mentor técnico:
  comparar alternativas, elegir la tecnología más adecuada para cada caso y
  contrastar enfoques para alcanzar la mejor solución posible.

El uso de estas herramientas refleja una competencia cada vez más relevante en
el desarrollo profesional: **saber dirigir y aprovechar la IA de forma
crítica**, manteniendo en todo momento el criterio técnico y la
responsabilidad sobre el resultado final.

---

## 🏗️ Arquitectura y decisiones de diseño

SmartCommerce sigue una arquitectura de **monolito modular** con capas bien
definidas. Cada módulo (auth, user, product, category, address, cart…) se
organiza por responsabilidad:

```
controller → service → repository
        dto · mapper · entity · security · config · exception
```

**Principios aplicados:**

- Nunca se exponen entidades: toda entrada/salida usa **DTOs**.
- Controladores delgados; la lógica de negocio vive en los **servicios**.
- Mapeo entidad ↔ DTO con **MapStruct**.
- Validación con **Jakarta Validation**.
- Seguridad **stateless** con JWT y roles (`USER`, `ADMIN`).

### ¿Por qué estas decisiones (y no otras)?

| Decisión | Por qué | Alternativa descartada |
|---|---|---|
| **Monolito modular** | El dominio es un único e-commerce; un monolito bien modularizado da orden y velocidad sin la complejidad operativa de desplegar y comunicar muchos servicios. Los módulos quedan desacoplados y extraíbles a futuro. | **Microservicios**: sobredimensionado para el alcance (despliegues independientes, red, consistencia distribuida). |
| **Arquitectura por capas** | Separa responsabilidades y facilita localizar, testear y mantener el código. | Lógica en los controladores: acopla y dificulta el mantenimiento. |
| **PostgreSQL (relacional)** | El dominio es fuertemente relacional (usuarios, productos, carritos, direcciones) y necesita integridad referencial y transacciones. | **NoSQL**: no encaja con datos transaccionales y relacionales. |
| **JWT (stateless)** | API sin estado, escalable y desacoplada del frontend (SPA); sin estado de sesión en servidor. | Sesiones en servidor: menos escalables y más acopladas. |
| **MapStruct** | Genera el mapeo en tiempo de compilación (sin reflexión en runtime): rápido, menos *boilerplate* y menos errores. | Mapeo manual: repetitivo y propenso a fallos. |
| **DTOs (no exponer entidades)** | Protege el modelo interno y define un contrato de API estable. | Exponer entidades: filtra el modelo y crea acoplamiento. |
| **Frontend React desacoplado** | SPA independiente que consume la API; permite desarrollar, desplegar y escalar front y back por separado. | Renderizado en servidor / plantillas: acopla vista y backend. |

El repositorio es un **monorepo**: backend y frontend conviven en el mismo
proyecto para facilitar su orquestación conjunta con Docker.

---

## 🛠️ Tecnologías

### Backend
- **Java 21**
- **Spring Boot 4** (Web MVC, Data JPA, Security, Validation)
- **Spring Security** + **JWT** (jjwt) — autenticación stateless
- **Spring Data JPA / Hibernate**
- **MapStruct** — mapeo entidad ↔ DTO
- **Lombok** — reducción de *boilerplate*
- **springdoc-openapi (Swagger UI)** — documentación de la API
- **Maven** — gestión de dependencias y build

### Frontend *(en desarrollo)*
- **React** con **Vite**
- **JavaScript**
- **Tailwind CSS** — estilos
- **React Router** — navegación (SPA)
- **axios** — cliente HTTP con interceptores para el JWT
- **useState / useEffect** — gestión de estado y carga de datos

### Base de datos
- **PostgreSQL 15**

### Infraestructura
- **Docker / Docker Compose**

---

## 📂 Estructura del proyecto

```
SmartCommerce/
└── smartcommerce/                      # Repositorio (backend Spring Boot)
    ├── src/main/java/com/smartcommerce/
    │   ├── auth/                        # Registro e inicio de sesión (JWT)
    │   ├── user/                        # Perfil de usuario y gestión admin
    │   ├── product/                    # Productos (CRUD, filtros, imágenes)
    │   ├── productimage/               # Imágenes de producto
    │   ├── category/                   # Categorías jerárquicas
    │   ├── address/                    # Direcciones del usuario
    │   ├── cart/                       # Carrito y líneas de carrito
    │   ├── security/                   # JwtAuthFilter, SecurityConfig, JwtService
    │   ├── config/                     # Inicialización (seed de admin)
    │   └── exception/                  # Manejo global de excepciones
    │   # Cada módulo: controller · service(+impl) · repository · dto · mapper · entity
    ├── src/main/resources/
    │   └── application.properties      # Configuración de la aplicación
    ├── src/test/                       # Tests
    ├── frontend/                       # Cliente web (React + Vite)
    │   └── src/  → api · services · context · hooks · components · pages · layouts · routes
    ├── docs/                           # Documentación de diseño y planes
    ├── docker-compose.yml              # Orquestación de servicios
    └── pom.xml                         # Dependencias y build del backend
```

---

## 🗄️ Base de datos

Motor: **PostgreSQL 15**. El esquema se genera automáticamente a partir de las
entidades JPA (`spring.jpa.hibernate.ddl-auto=update`).

### Parámetros de conexión (por defecto)

| Parámetro | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `5432` |
| Base de datos | `smartcommerce` |
| Usuario | `postgres` |
| Contraseña | `postgres` |
| URL JDBC | `jdbc:postgresql://localhost:5432/smartcommerce` |

> Estos valores se pueden modificar en `src/main/resources/application.properties`.

### Tablas principales

| Tabla | Descripción |
|---|---|
| **users** | Usuarios de la aplicación: nombre, email (único), contraseña cifrada, rol (`USER`/`ADMIN`), estado activo y fecha de alta. |
| **products** | Catálogo de productos: nombre, descripción, precio, stock, estado, marca y categoría. |
| **product_images** | Imágenes asociadas a cada producto (relación 1:N con `products`). |
| **categories** | Categorías **jerárquicas** (autorreferenciadas vía `parent_id`), con `slug` único y estado activo. |
| **addresses** | Direcciones de cada usuario (calle, ciudad, provincia, código postal, país) y marca de dirección predeterminada. |
| **cart** | Carrito de la compra, uno por usuario (relación 1:1 con `users`). |
| **cart_items** | Líneas del carrito: producto y cantidad (relación N:1 con `cart` y `products`). |

**Relaciones clave:** `users 1—N addresses`, `users 1—1 cart`, `cart 1—N
cart_items`, `products 1—N product_images`, `categories 1—N categories`
(jerarquía), `products N—1 categories`.

---

## ⚙️ Configuración

Toda la configuración está centralizada en
`src/main/resources/application.properties`:

```properties
# Base de datos
spring.datasource.url=jdbc:postgresql://localhost:5432/smartcommerce
spring.datasource.username=postgres
spring.datasource.password=postgres

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update

# JWT
jwt.secret=<clave-secreta>
jwt.expiration=3600000            # duración del token en ms (1 hora)

# Usuario administrador inicial (seed)
app.admin.email=admin@smartcommerce.com
app.admin.password=Admin1234!
```

> Para un entorno real, estos valores (credenciales de BD, `jwt.secret`, etc.)
> deberían externalizarse mediante variables de entorno en lugar de dejarse en
> el fichero de propiedades.

---

## 🚀 Puesta en marcha y compilación

### Requisitos
- Java 21
- Maven (incluido el wrapper `./mvnw`)
- Node.js 18+ y npm
- Docker (para la base de datos)

### 1. Clonar el repositorio
```bash
git clone <url-del-repositorio>
cd smartcommerce
```

### 2. Levantar la base de datos (PostgreSQL vía Docker)
```bash
docker compose up -d
```

### 3. Backend
```bash
# Compilar y empaquetar
./mvnw clean install

# Ejecutar
./mvnw spring-boot:run
```
API disponible en `http://localhost:8080`.

**Usuario administrador por defecto:**
- Email: `admin@smartcommerce.com`
- Contraseña: `Admin1234!`

### 4. Frontend
```bash
cd frontend
npm install
npm run dev          # desarrollo (http://localhost:5173)
npm run build        # build de producción
```
El frontend llama directamente al backend usando la variable de entorno
`VITE_API_URL` (por defecto `http://localhost:8080/api/v1`). El backend habilita
**CORS** para autorizar el origen del frontend.

---

## ✅ Funcionalidades

### Funcionalidades generales
- Registro e inicio de sesión con **JWT**.
- Control de acceso por **roles** (`USER` / `ADMIN`).
- Catálogo de productos con filtros y paginación.
- Carrito de la compra con validación de stock.
- Gestión de perfil y direcciones del usuario.
- Documentación interactiva de la API (Swagger).

### Por sección / módulo

| Módulo | Funcionalidad |
|---|---|
| **Auth** | Registro y login; emisión de token JWT. |
| **Usuarios** | Consulta y edición del perfil propio; gestión de administrador (listar, consultar, desactivar y promover usuarios a admin). |
| **Productos** | CRUD de administrador; listado público paginado con filtros; detalle; gestión de imágenes. |
| **Categorías** | CRUD de categorías jerárquicas (categorías y subcategorías). |
| **Direcciones** | CRUD de direcciones del usuario y gestión de la dirección predeterminada. |
| **Carrito** | Añadir productos, actualizar cantidad, eliminar y vaciar; validación de stock y de estado del producto. |
| **Seguridad** | Filtro JWT, endpoints de administrador protegidos, manejo global de errores con respuestas consistentes. |

---

## 📚 Documentación de la API

Con el backend en marcha, la documentación interactiva (Swagger UI) está
disponible en:

```
http://localhost:8080/swagger-ui.html
```

---

## 🐳 Docker

Actualmente `docker-compose.yml` levanta la **base de datos PostgreSQL**.

**Visión (objetivo):** contenerizar **toda la aplicación** —base de datos,
backend y frontend— con Docker Compose, de forma que cualquier persona pueda
clonar el repositorio y ejecutar la aplicación completa con un único comando:

```bash
docker compose up
```

Sin necesidad de instalar Java, Node, PostgreSQL ni ninguna otra dependencia en
la máquina. El objetivo es que la aplicación sea **fácilmente ejecutable en
cualquier dispositivo, sin configuración previa**.

---

## 👤 Autor

**Darius Adrian Benta**
Proyecto de portfolio — backend e-commerce con Spring Boot y React.

- 💼 LinkedIn: [dariusadrianbenta](https://www.linkedin.com/in/dariusadrianbenta/)
- 🐙 GitHub: [DariusAdrianBenta](https://github.com/DariusAdrianBenta)
- 🌐 Portfolio: [portfolio-darius-benta.vercel.app](https://portfolio-darius-benta.vercel.app/)
