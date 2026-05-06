const express = require('express');
const router = express.Router();
const pool = require('../config/db');

/**
 * POST /api/haccp/registro-sensor
 * Ruta de alta velocidad para dispositivos IoT. 
 * Ingresa mediciones de temperatura cada X minutos.
 */
router.post('/registro-sensor', async (req, res) => {
    const { idPunto, valorMedido, fuente = 'sensor_iot', idUsuario = null } = req.body;

    if (!idPunto || valorMedido === undefined) {
        return res.status(400).json({ error: 'idPunto y valorMedido son obligatorios' });
    }

    try {
        // En PostgreSQL no podemos usar un CALL a SP para obtener el RETURNING directamente si el SP no devuelve una tabla.
        // Pero nuestro diseño de BD registra en haccp_registros_monitoreo y el Trigger func_nc_haccp_desviacion() levanta la NC si es necesario.
        
        // Vamos a insertar directamente en la bitácora
        // Obtenemos los límites primero para insertarlos como snapshot
        const puntoRes = await pool.query('SELECT Limite_Critico_Min, Limite_Critico_Max FROM haccp_puntos_criticos WHERE ID_Punto = $1', [idPunto]);
        
        if (puntoRes.rows.length === 0) {
            return res.status(404).json({ error: 'Punto Crítico no encontrado' });
        }

        const limites = puntoRes.rows[0];

        const insertRes = await pool.query(`
            INSERT INTO haccp_registros_monitoreo 
            (ID_Punto, Valor_Medido, Fuente, ID_Usuario, Limite_Critico_Min, Limite_Critico_Max)
            VALUES ($1, $2, $3, $4, $5, $6)
            RETURNING ID_Registro, Desviacion
        `, [idPunto, valorMedido, fuente, idUsuario, limites.limite_critico_min, limites.limite_critico_max]);

        const registro = insertRes.rows[0];

        res.status(201).json({
            success: true,
            mensaje: 'Medición registrada',
            alerta: registro.desviacion ? 'DESVIACIÓN DETECTADA. No Conformidad generada por Trigger.' : 'Normal',
            registro
        });

    } catch (error) {
        console.error('Error in IoT ingest:', error);
        res.status(500).json({ error: 'Error procesando medición HACCP' });
    }
});

/**
 * GET /api/haccp/alertas
 * Obtiene las desviaciones críticas (NC automáticas) activas.
 */
router.get('/alertas', async (req, res) => {
    try {
        const result = await pool.query('SELECT * FROM v_alertas_haccp');
        res.json({
            success: true,
            totalAlertas: result.rows.length,
            data: result.rows
        });
    } catch (error) {
        console.error('Error fetching HACCP alerts:', error);
        res.status(500).json({ error: 'Error obteniendo alertas HACCP' });
    }
});

module.exports = router;
