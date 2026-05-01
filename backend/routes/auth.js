const express = require('express');
const router = express.Router();
const db = require('../config/database');

router.post('/login', async (req, res, next) => {
    try {
        const { correo, contrasena } = req.body;
        
        // Since passwords are SHA2(..., 256), we compare exactly that
        const [users] = await db.query(
            'SELECT * FROM usuarios WHERE Correo = ? AND Contrasena = SHA2(?, 256)',
            [correo, contrasena]
        );

        if (users.length === 0) {
            return res.status(401).json({ success: false, message: 'Invalid credentials' });
        }

        const user = users[0];
        // In a real app we would generate a JWT token here
        res.json({ 
            success: true, 
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
