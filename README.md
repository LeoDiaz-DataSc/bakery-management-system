# 🍞 Bakery Management System — Sistema de Gestión de Panadería

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?logo=apachemaven)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](./LICENSE)

A full-featured **Bakery Management System** built with Java Swing that manages products, ingredients, recipes, sales, purchases, production costs, and user authentication with role-based access control.

---

## 📸 Screenshots

> _Screenshots coming soon — the application features a modern FlatLaf Look & Feel with a dashboard, product management, sales tracking, and production cost analysis._

---

## 🎯 Features

### Core Modules
| Module | Description |
|--------|-------------|
| 🔐 **Authentication** | Login system with 3 roles: `admin`, `employee`, `inventory` |
| 📊 **Dashboard** | Real-time statistics and JFreeChart visualizations |
| 🍰 **Products** | Full CRUD for bakery products with categories |
| 🥚 **Ingredients** | Ingredient inventory management with minimum stock alerts |
| 📋 **Recipes** | Link products to ingredients with quantity requirements |
| 🏷️ **Categories** | Product categorization |
| 🚚 **Suppliers** | Supplier contact and delivery management |
| 💰 **Sales** | Point of sale with payment method tracking |
| 🛒 **Purchases** | Ingredient procurement from suppliers |
| 📈 **Production Costs** | Automated cost calculation per product |
| 👥 **Users** | User management panel (admin only) |

### Architecture & Design Patterns
- **MVC Pattern** — Model, View, Controller separation
- **DAO Pattern** — Data Access Objects for database operations
- **Observer Pattern** — `DataChangeListener` / `DataChangeNotifier` for real-time UI sync
- **Singleton** — Database connection management
- **Modern UI** — FlatLaf Look & Feel for a polished interface

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 21 |
| **Build** | Maven |
| **GUI** | Swing + FlatLaf 3.2.1 |
| **Charts** | JFreeChart 1.5.4 |
| **Date Picker** | JCalendar 1.4 |
| **Database** | MySQL 8.0 |
| **Connector** | mysql-connector-java 8.0.33 |

---

## 📁 Project Structure

```
bakery-management-system/
├── PanaderiaCRUD/
│   ├── pom.xml
│   ├── diccionario_datos.txt          # Data dictionary documentation
│   └── src/main/java/com/mycompany/panaderiacrud/
│       ├── PanaderiaCRUD.java         # Application entry point
│       ├── gui/
│       │   ├── LoginFrame.java        # Authentication window
│       │   ├── MainFrame.java         # Main application frame
│       │   ├── DashboardPanel.java    # Statistics & charts
│       │   ├── ProductosPanel.java    # Products CRUD
│       │   ├── IngredientesPanel.java # Ingredients CRUD
│       │   ├── CategoriasPanel.java   # Categories CRUD
│       │   ├── ProveedoresPanel.java  # Suppliers CRUD
│       │   ├── RecetasPanel.java      # Recipes management
│       │   ├── VentasPanel.java       # Sales tracking
│       │   ├── ComprasPanel.java      # Purchases tracking
│       │   ├── CostosProduccionPanel.java  # Cost analysis
│       │   └── UsuariosPanel.java     # User management
│       ├── model/
│       │   └── UsuarioDAO.java        # User data access
│       └── util/
│           ├── ConexionDB.java        # Database connection
│           ├── DataChangedEvent.java  # Observer event
│           ├── DataChangeListener.java # Observer interface
│           └── DataChangeNotifier.java # Observer notifier
├── Panaderia.sql                      # Database schema + seed data
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- Java 21 (JDK)
- Maven 3.x
- MySQL 8.0

### 1. Set up the Database
```bash
mysql -u root -p < Panaderia.sql
```

### 2. Configure Database Connection
Update `ConexionDB.java` with your MySQL credentials.

### 3. Build & Run
```bash
cd PanaderiaCRUD
mvn clean compile exec:java
```

---

## 📊 Database Schema

**11 tables** with full referential integrity:

| Table | Records | Purpose |
|-------|---------|---------|
| `productos` | Products catalog |
| `ingredientes` | Raw ingredients inventory |
| `recetas` | Product-ingredient recipes |
| `categorias` | Product categories |
| `proveedores` | Supplier directory |
| `ventas` | Sales transactions |
| `detalles_venta` | Sale line items |
| `compras` | Purchase orders |
| `detalles_compra` | Purchase line items |
| `costos_produccion` | Production cost tracking |
| `usuarios` | System users with roles |

---

## 🔮 Roadmap

- [ ] Web version with React frontend + Node.js API
- [ ] Docker containerization
- [ ] Cloud database migration
- [ ] REST API documentation
- [ ] Unit tests (JUnit)

---

## 📄 License

This project is licensed under the MIT License — see [LICENSE](./LICENSE) for details.

---

**Developed by [Leonardo Diaz](https://github.com/LeoDiaz-DataSc)**
