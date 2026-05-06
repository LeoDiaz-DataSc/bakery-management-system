require('dotenv').config();
const express = require('express');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json()); // Necesario para parsear el JSON de los sensores IoT

// Rutas API
app.use('/api/haccp', require('./routes/haccp'));
app.use('/api/trazabilidad', require('./routes/recall'));
app.use('/api/inventario', require('./routes/inventory'));

// Ruta base
app.get('/', (req, res) => {
    res.json({ message: 'Panadería Enterprise API (PostgreSQL) - Running' });
});

app.listen(PORT, () => {
    console.log(`[SERVER] Servidor Node.js corriendo en el puerto ${PORT}`);
    console.log(`[SERVER] API lista para recibir pings IoT en /api/haccp/registro-sensor`);
});
