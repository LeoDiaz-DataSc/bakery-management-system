const express = require('express');
const router = express.Router();
const db = require('../config/database');

// GET all sales
router.get('/', async (req, res, next) => {
    try {
        const [ventas] = await db.query('SELECT * FROM ventas ORDER BY Fecha_Venta DESC');
        res.json({ success: true, count: ventas.length, data: ventas });
    } catch (err) {
        next(err);
    }
});

// POST new sale
router.post('/', async (req, res, next) => {
    const connection = await db.getConnection();
    try {
        await connection.beginTransaction();

        const { Cliente, Metodo_Pago, Total, Detalles } = req.body;
        
        // 1. Insert Venta
        const [ventaResult] = await connection.query(
            'INSERT INTO ventas (Cliente, Total, Metodo_Pago) VALUES (?, ?, ?)',
            [Cliente, Total, Metodo_Pago]
        );
        const idVenta = ventaResult.insertId;

        // 2. Insert Detalles
        for (const item of Detalles) {
            await connection.query(
                'INSERT INTO detalles_venta (ID_Venta, ID_Producto, Cantidad, Precio_Unitario, Subtotal) VALUES (?, ?, ?, ?, ?)',
                [idVenta, item.ID_Producto, item.Cantidad, item.Precio_Unitario, item.Subtotal]
            );
            
            // Deduct stock
            await connection.query(
                'UPDATE productos SET Stock_Disponible = Stock_Disponible - ? WHERE ID_PRODUCTO = ?',
                [item.Cantidad, item.ID_Producto]
            );
        }

        await connection.commit();
        res.status(201).json({ success: true, idVenta });
    } catch (err) {
        await connection.rollback();
        next(err);
    } finally {
        connection.release();
    }
});

module.exports = router;
