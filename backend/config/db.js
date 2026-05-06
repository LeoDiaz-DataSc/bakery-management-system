const { Pool } = require('pg');
require('dotenv').config();

// Pool de conexiones para PostgreSQL optimizado para alta concurrencia (IoT)
const pool = new Pool({
    user: process.env.DB_USER || 'postgres',
    host: process.env.DB_HOST || 'localhost',
    database: process.env.DB_NAME || 'panaderia',
    password: process.env.DB_PASSWORD || 'root',
    port: process.env.DB_PORT || 5432,
    max: 20, // max número de clientes en el pool
    idleTimeoutMillis: 30000,
    connectionTimeoutMillis: 2000,
});

pool.on('error', (err, client) => {
    console.error('Unexpected error on idle client', err);
    process.exit(-1);
});

module.exports = pool;
