const express = require('express');
const router = express.Router();
const db = require('../config/database');

// GET all products with categories
router.get('/', async (req, res, next) => {
    try {
        const query = `
            SELECT p.*, c.Nombre_Categoria 
            FROM productos p 
            LEFT JOIN categorias c ON p.ID_Categoria = c.ID_Categoria
            WHERE p.Estado = 'activo'
        `;
        const [productos] = await db.query(query);
        res.json({ success: true, count: productos.length, data: productos });
    } catch (err) {
        next(err);
    }
});

// POST new product
router.post('/', async (req, res, next) => {
    try {
        const { Nom_Producto, descripcion, Precio_Unidad, ID_Categoria } = req.body;
        const [result] = await db.query(
            'INSERT INTO productos (Nom_Producto, descripcion, Precio_Unidad, ID_Categoria) VALUES (?, ?, ?, ?)',
            [Nom_Producto, descripcion, Precio_Unidad, ID_Categoria]
        );
        res.status(201).json({ success: true, id: result.insertId });
    } catch (err) {
        next(err);
    }
});

// GET all categories
router.get('/categorias', async (req, res, next) => {
    try {
        const [categorias] = await db.query('SELECT * FROM categorias');
        res.json({ success: true, data: categorias });
    } catch (err) {
        next(err);
    }
});

module.exports = router;
