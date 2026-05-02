const db = require('../config/database');

const logAction = (accion) => {
    return async (req, res, next) => {
        // Intercept the original res.json to log after successful response
        const originalJson = res.json;
        res.json = function (data) {
            if (res.statusCode >= 200 && res.statusCode < 300) {
                const userId = req.user ? req.user.id : null;
                const ip = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
                const detalle = JSON.stringify(req.body).substring(0, 500); // Guardar un extracto del body

                db.query(
                    'INSERT INTO Audit_Logs (ID_Usuario, Accion, Detalle, Direccion_IP) VALUES (?, ?, ?, ?)',
                    [userId, accion, detalle, ip]
                ).catch(err => console.error("Error saving audit log", err));
            }
            return originalJson.call(this, data);
        };
        next();
    };
};

module.exports = logAction;
