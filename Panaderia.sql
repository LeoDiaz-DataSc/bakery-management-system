drop database panaderia;
create database panaderia;
use panaderia;
---

CREATE TABLE productos (
    ID_PRODUCTO INT PRIMARY KEY AUTO_INCREMENT,
    Nom_Producto VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    Precio_Unidad DECIMAL(10,2) NOT NULL,
    Stock_Disponible INT DEFAULT 0,
    Fecha_Vencimiento DATE,
    Estado ENUM('activo', 'inactivo') DEFAULT 'activo',
    ID_Categoria INT,
    FOREIGN KEY (ID_Categoria) REFERENCES categorias(ID_Categoria)
) ENGINE=InnoDB;


---
CREATE TABLE ingredientes (
    ID_Ingrediente INT PRIMARY KEY AUTO_INCREMENT,
    Nom_Ingrediente VARCHAR(100) NOT NULL UNIQUE,
    Unidad_Med VARCHAR(20),
    Costo_Unidad DECIMAL(10,2),
    Stock_Actual INT DEFAULT 0,
    Stock_Minimo INT DEFAULT 5,
    ID_Proveedor INT,
    FOREIGN KEY (ID_Proveedor) REFERENCES proveedores(ID_Proveedor)
) ENGINE=InnoDB;


---
CREATE TABLE recetas (
    ID_Receta INT PRIMARY KEY AUTO_INCREMENT,
    ID_Producto INT,
    ID_Ingrediente INT,
    Cantidad_Necesaria DECIMAL(10,2) NOT NULL,
    Instrucciones_Adicionales TEXT,
    UNIQUE (ID_Producto, ID_Ingrediente),
    FOREIGN KEY (ID_Producto) REFERENCES productos(ID_PRODUCTO),
    FOREIGN KEY (ID_Ingrediente) REFERENCES ingredientes(ID_Ingrediente)
) ENGINE=InnoDB;


---
CREATE TABLE categorias (
    ID_Categoria INT PRIMARY KEY AUTO_INCREMENT,
    Nombre_Categoria VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

---
CREATE TABLE proveedores (
    ID_Proveedor INT PRIMARY KEY AUTO_INCREMENT,
    Nombre_Proveedor VARCHAR(100) NOT NULL,
    Contacto VARCHAR(100),
    Telefono VARCHAR(20),
    Email VARCHAR(100),
    Direccion VARCHAR(255)
) ENGINE=InnoDB;

---
CREATE TABLE ventas (
    ID_Venta INT PRIMARY KEY AUTO_INCREMENT,
    Fecha_Venta DATETIME DEFAULT CURRENT_TIMESTAMP,
    Total DECIMAL(10,2) NOT NULL,
    Metodo_Pago ENUM('efectivo', 'tarjeta', 'transferencia')
) ENGINE=InnoDB;


---
CREATE TABLE detalles_venta (
    ID_Detalle INT PRIMARY KEY AUTO_INCREMENT,
    ID_Venta INT,
    ID_Producto INT,
    Cantidad INT NOT NULL,
    Precio_Unitario DECIMAL(10,2) NOT NULL,
    Subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (ID_Venta) REFERENCES ventas(ID_Venta),
    FOREIGN KEY (ID_Producto) REFERENCES productos(ID_PRODUCTO)
) ENGINE=InnoDB;


---
CREATE TABLE compras (
    ID_Compra INT PRIMARY KEY AUTO_INCREMENT,
    ID_Proveedor INT,
    Fecha_Compra DATETIME DEFAULT CURRENT_TIMESTAMP,
    Total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (ID_Proveedor) REFERENCES proveedores(ID_Proveedor)
) ENGINE=InnoDB;


---
CREATE TABLE detalles_compra (
    ID_DetalleCompra INT PRIMARY KEY AUTO_INCREMENT,
    ID_Compra INT,
    ID_Ingrediente INT,
    Cantidad DECIMAL(10,2) NOT NULL,
    Precio_Unitario DECIMAL(10,2) NOT NULL,
    Subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (ID_Compra) REFERENCES compras(ID_Compra),
    FOREIGN KEY (ID_Ingrediente) REFERENCES ingredientes(ID_Ingrediente)
) ENGINE=InnoDB;


---
CREATE TABLE costos_produccion (
    ID_CostoProduccion INT PRIMARY KEY AUTO_INCREMENT,
    ID_Producto INT,
    Costo_Total DECIMAL(10,2) NOT NULL,
    Fecha_Calculo DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ID_Producto) REFERENCES productos(ID_PRODUCTO)
) ENGINE=InnoDB;


---
CREATE TABLE usuarios (
    ID_Usuario INT PRIMARY KEY AUTO_INCREMENT,
    Nombre VARCHAR(50) NOT NULL,
    Apellido VARCHAR(50) NOT NULL,
    Correo VARCHAR(100) UNIQUE,
    Contrasena VARCHAR(255),
    Rol ENUM('admin', 'empleado', 'inventario') DEFAULT 'empleado'
) ENGINE=InnoDB;
ALTER TABLE ventas ADD COLUMN Cliente VARCHAR(100) AFTER Fecha_Venta;
-- Insertar usuario administrador
INSERT INTO usuarios (Nombre, Apellido, Correo, Contrasena, Rol)
VALUES (
    'Administrador',
    'Panaderia',
    'admin@panaderia.com',
    'Admin123', -- Contraseña: "Admin123" (cifrada)
    'admin'
);

-- Insertar empleado
INSERT INTO usuarios (Nombre, Apellido, Correo, Contrasena, Rol)
VALUES (
    'Empleado',
    'Panadero',
    'empleado@panaderia.com',
    'Empleado123', -- Contraseña: "Empleado123" (cifrada)
    'empleado'
);

-- Insertar encargado de inventario
INSERT INTO usuarios (Nombre, Apellido, Correo, Contrasena, Rol)
VALUES (
    'Inventario',
    'Encargado',
    'inventario@panaderia.com',
    'Inventario123', -- Contraseña: "Inventario123" (cifrada)
    'inventario'
);