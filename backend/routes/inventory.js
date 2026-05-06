const express = require('express');
const router = express.Router();
const pool = require('../config/db');

/**
 * GET /api/inventario/critico
 * Devuelve ingredientes que han caído por debajo del stock mínimo.
 */
router.get('/critico', async (req, res) => {
    try {
        const result = await pool.query('SELECT * FROM v_alertas_stock');
        res.json({
            success: true,
            alertas: result.rows.length,
            data: result.rows
        });
    } catch (error) {
        console.error('Error fetching stock alerts:', error);
        res.status(500).json({ error: 'Error obteniendo alertas de stock' });
    }
});

/**
 * GET /api/inventario/caducidad
 * Devuelve lotes de ingredientes que están a punto de vencer (próximos 7 días).
 */
router.get('/caducidad', async (req, res) => {
    try {
        const result = await pool.query('SELECT * FROM v_lotes_por_vencer');
        res.json({
            success: true,
            alertas: result.rows.length,
            data: result.rows
        });
    } catch (error) {
        console.error('Error fetching expiry alerts:', error);
        res.status(500).json({ error: 'Error obteniendo alertas de caducidad' });
    }
});

module.exports = router;
