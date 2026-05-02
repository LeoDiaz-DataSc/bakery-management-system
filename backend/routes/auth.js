const express = require('express');
const router = express.Router();
const db = require('../config/database');
const jwt = require('jsonwebtoken');

router.post('/login', async (req, res, next) => {
    try {
        const { correo, contrasena } = req.body;
        
        const [users] = await db.query(
            'SELECT * FROM usuarios WHERE Correo = ? AND Contrasena = SHA2(?, 256)',
            [correo, contrasena]
        );

        const ip = req.headers['x-forwarded-for'] || req.socket.remoteAddress;

        if (users.length === 0) {
            await db.query('INSERT INTO Audit_Logs (Accion, Detalle, Direccion_IP) VALUES (?, ?, ?)', 
                ['LOGIN_FAILED', `Attempt for email: ${correo}`, ip]);
            return res.status(401).json({ success: false, message: 'Invalid credentials' });
        }

        const user = users[0];
        
        const token = jwt.sign(
            { id: user.ID_Usuario, rol: user.Rol, correo: user.Correo },
            process.env.JWT_SECRET,
            { expiresIn: '8h' }
        );

        await db.query('INSERT INTO Audit_Logs (Accion, Detalle, Direccion_IP) VALUES (?, ?, ?)', 
            ['LOGIN_SUCCESS', `User ${user.Correo} logged in`, ip]);

        res.json({ 
            success: true,
            token, 
            user: { 
                id: user.ID_Usuario, 
                nombre: user.Nombre, 
                rol: user.Rol 
            } 
        });
    } catch (err) {
        next(err);
    }
});

module.exports = router;
