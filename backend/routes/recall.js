const express = require('express');
const router = express.Router();
const pool = require('../config/db');

/**
 * GET /api/trazabilidad/recall/:loteProveedor
 * Motor de Recall (ISO 22000). Encuentra a qué clientes se les vendió producto elaborado 
 * con un lote específico de proveedor que se ha reportado como contaminado.
 */
router.get('/recall/:loteProveedor', async (req, res) => {
    const { loteProveedor } = req.params;

    try {
        // En PostgreSQL no podemos usar 'CALL sp_recall_por_lote' y obtener el Select result directamente
        // porque en el PL/pgSQL actual solo hicimos un SELECT dentro, pero en Postgres un PROCEDURE 
        // no devuelve sets de datos de esa forma. 
        // Es mejor consultar la vista v_recall_ingrediente directamente desde Node.
        
        const result = await pool.query(`
            SELECT * FROM v_recall_ingrediente 
            WHERE lote_proveedor = $1
            ORDER BY fecha_venta
        `, [loteProveedor]);

        res.json({
            success: true,
            loteAfectado: loteProveedor,
            clientesAfectados: result.rows.length,
            data: result.rows
        });

    } catch (error) {
        console.error('Error in Recall engine:', error);
        res.status(500).json({ error: 'Error ejecutando motor de Recall' });
    }
});

module.exports = router;
