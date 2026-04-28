# Bakery Management System

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?logo=apachemaven)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](./LICENSE)

## Overview

This project implements a comprehensive bakery management system as a desktop application built with Java Swing. The system provides complete operational coverage including product inventory, ingredient tracking, recipe management, point-of-sale operations, supplier procurement, production cost analysis, and multi-role user authentication. The graphical interface leverages the FlatLaf Look and Feel framework and integrates JFreeChart for real-time statistical dashboard rendering.

## System Architecture

The application follows a layered architecture with clear separation of concerns:

```
Presentation Layer       GUI panels (Swing + FlatLaf)
Business Logic Layer     Event-driven data synchronization (Observer pattern)
Data Access Layer        DAO classes with prepared statements
Infrastructure Layer     MySQL connection management (Singleton)
```

### Design Patterns

| Pattern | Implementation | Purpose |
|---------|---------------|---------|
| **MVC** | `model/` + `gui/` + `util/` packages | Separation of domain logic, presentation, and data access |
| **DAO** | `UsuarioDAO.java`, inline DAO operations | Abstraction of database operations from business logic |
| **Observer** | `DataChangeListener`, `DataChangeNotifier`, `DataChangedEvent` | Real-time UI synchronization across panels |
| **Singleton** | `ConexionDB.java` | Single database connection instance |

## Functional Modules

| Module | Description |
|--------|-------------|
| **Authentication** | Login with 3 roles: `admin`, `empleado`, `inventario` |
| **Dashboard** | JFreeChart visualizations: revenue metrics, inventory status |
| **Products** | CRUD with categories, stock tracking, expiration dates |
| **Ingredients** | Inventory with minimum stock alerts and unit pricing |
| **Recipes** | Product-ingredient linkage with quantity requirements |
| **Categories** | Product classification taxonomy |
| **Suppliers** | Supplier directory and contact management |
| **Sales** | Point-of-sale with payment method tracking |
| **Purchases** | Ingredient procurement from suppliers |
| **Production Costs** | Automated cost computation per product |
| **User Administration** | User management (admin-only) |

## Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 21 |
| Build | Apache Maven | 3.x |
| GUI | Swing + FlatLaf | 3.2.1 |
| Charts | JFreeChart | 1.5.4 |
| Database | MySQL | 8.0 |
| JDBC | mysql-connector-java | 8.0.33 |

## Database Schema

11 tables with full referential integrity: `productos`, `ingredientes`, `recetas`, `categorias`, `proveedores`, `ventas`, `detalles_venta`, `compras`, `detalles_compra`, `costos_produccion`, `usuarios`.

## Installation and Execution

### Prerequisites
- JDK 21, Maven 3.x, MySQL 8.0

```bash
mysql -u root -p < Panaderia.sql
cd PanaderiaCRUD
mvn clean compile exec:java
```

## Roadmap

- [ ] Service layer extraction between DAO and GUI
- [ ] Web frontend (React) with Node.js REST API
- [ ] Docker containerization
- [ ] Unit tests (JUnit 5)
- [ ] BCrypt password hashing
- [ ] Cloud database migration

## License

MIT License. See [LICENSE](./LICENSE).

**Developed by [Leonardo Diaz](https://github.com/LeoDiaz-DataSc)**

---

# Version en Espanol

## Descripcion General

Este proyecto implementa un sistema integral de gestion de panaderia como aplicacion de escritorio construida con Java Swing. Proporciona cobertura operativa completa: inventario de productos, seguimiento de ingredientes, gestion de recetas, punto de venta, adquisiciones de proveedores, analisis de costos de produccion y autenticacion con multiples roles. La interfaz grafica utiliza FlatLaf e integra JFreeChart para dashboards estadisticos en tiempo real.

## Arquitectura del Sistema

Arquitectura por capas con separacion de responsabilidades:

```
Capa de Presentacion         Paneles GUI (Swing + FlatLaf)
Capa de Logica de Negocio    Sincronizacion por eventos (patron Observer)
Capa de Acceso a Datos       Clases DAO con sentencias preparadas
Capa de Infraestructura      Gestion de conexion MySQL (Singleton)
```

### Patrones de Diseno

| Patron | Implementacion | Proposito |
|--------|---------------|-----------|
| **MVC** | Paquetes `model/` + `gui/` + `util/` | Separacion de dominio, presentacion y acceso a datos |
| **DAO** | `UsuarioDAO.java` | Abstraccion de operaciones de base de datos |
| **Observador** | `DataChangeListener`, `DataChangeNotifier` | Sincronizacion de UI en tiempo real |
| **Singleton** | `ConexionDB.java` | Instancia unica de conexion |

## Modulos Funcionales

| Modulo | Descripcion |
|--------|-------------|
| **Autenticacion** | Login con 3 roles: `admin`, `empleado`, `inventario` |
| **Dashboard** | Visualizaciones JFreeChart: metricas de ingresos, estado de inventario |
| **Productos** | CRUD con categorias, seguimiento de stock, fechas de vencimiento |
| **Ingredientes** | Inventario con alertas de stock minimo |
| **Recetas** | Vinculacion producto-ingrediente con cantidades |
| **Ventas** | Punto de venta con metodos de pago |
| **Compras** | Adquisicion de ingredientes de proveedores |
| **Costos de Produccion** | Computo automatizado de costos por producto |
| **Administracion de Usuarios** | Gestion de usuarios (solo administradores) |

## Instalacion y Ejecucion

### Requisitos Previos
- JDK 21, Maven 3.x, MySQL 8.0

```bash
mysql -u root -p < Panaderia.sql
cd PanaderiaCRUD
mvn clean compile exec:java
```

## Hoja de Ruta

- [ ] Capa de servicio entre DAO y GUI
- [ ] Frontend web (React) con API REST Node.js
- [ ] Contenedorizacion con Docker
- [ ] Pruebas unitarias (JUnit 5)
- [ ] Hashing BCrypt para contrasenas
- [ ] Migracion a base de datos en la nube

**Desarrollado por [Leonardo Diaz](https://github.com/LeoDiaz-DataSc)**
