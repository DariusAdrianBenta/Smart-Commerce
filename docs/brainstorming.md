# SmartCommerce – Especificación del Proyecto

## Objetivo

SmartCommerce es un backend e-commerce inspirado en Amazon,
desarrollado para simular un sistema backend profesional y escalable.

El proyecto busca aplicar:
- arquitectura limpia
- reglas de negocio reales
- desarrollo modular
- buenas prácticas backend

---

## Stack Tecnológico

- Java 21
- Spring Boot
- PostgreSQL
- Spring Security
- JWT
- JPA/Hibernate
- MapStruct
- Docker

---

## Arquitectura

La aplicación sigue una arquitectura:
- modular monolith
- layered architecture

Capas:
- controller
- service
- repository
- dto
- mapper
- entity
- security
- config
- exception

---

## Principios de Desarrollo

- Nunca exponer entidades
- Controllers delgados
- Lógica de negocio en services
- Uso obligatorio de DTOs
- Uso de MapStruct
- Validaciones con Jakarta Validation

---

## Reglas de Negocio Importantes

- Orders usan snapshot
- Cart dinámico
- Wishlist ManyToMany
- Categorías jerárquicas
- Validación de stock
- JWT stateless

---

## Estrategia de Desarrollo

El proyecto se desarrolla mediante vertical slices:
- feature por feature
- implementación end-to-end
- commits pequeños