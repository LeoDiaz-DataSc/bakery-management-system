const jwt = require('jsonwebtoken');

const verifyToken = (req, res, next) => {
    const token = req.headers['authorization'];
    
    if (!token) {
        return res.status(403).json({ success: false, message: 'Un token de acceso es requerido' });
    }

    try {
        const decoded = jwt.verify(token.replace('Bearer ', ''), process.env.JWT_SECRET);
        req.user = decoded;
    } catch (err) {
        return res.status(401).json({ success: false, message: 'Token inválido o expirado' });
    }
    
    return next();
};

const checkPermission = (requiredPermission) => {
    return async (req, res, next) => {
        const db = require('../config/database');
        try {
            const [permisos] = await db.query(`
                SELECT p.Nombre_Permiso 
                FROM Permisos p
                JOIN Rol_Permisos rp ON p.ID_Permiso = rp.ID_Permiso
                WHERE rp.ID_Rol = ? AND p.Nombre_Permiso = ?
            `, [req.user.rolId, requiredPermission]);

            if (permisos.length === 0) {
                return res.status(403).json({ success: false, message: 'No tienes permisos para realizar esta acción' });
            }
            next();
        } catch (error) {
            next(error);
        }
    };
};

module.exports = { verifyToken, checkPermission };
