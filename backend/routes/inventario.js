const express = require('express');
const router = express.Router();
const db = require('../config/database');
const { verifyToken } = require('../middleware/auth');
const logAction = require('../middleware/audit');

// Protegemos todas las rutas
router.use(verifyToken);

// GET all ingredients with providers
router.get('/', async (req, res, next) => {
    try {
        const query = `
            SELECT i.*, p.Nombre_Proveedor 
            FROM ingredientes i 
            LEFT JOIN proveedores p ON i.ID_Proveedor = p.ID_Proveedor
        `;
        const [ingredientes] = await db.query(query);
        res.json({ success: true, count: ingredientes.length, data: ingredientes });
    } catch (err) {
        next(err);
    }
});

module.exports = router;
