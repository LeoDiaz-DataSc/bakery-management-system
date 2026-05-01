import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:3000/api',
    timeout: 10000,
});

export const login = async (correo, contrasena) => {
    const response = await api.post('/auth/login', { correo, contrasena });
    return response.data;
};

export const getProductos = async () => {
    const response = await api.get('/productos');
    return response.data;
};

export const getInventario = async () => {
    const response = await api.get('/inventario');
    return response.data;
};

export const getVentas = async () => {
    const response = await api.get('/ventas');
    return response.data;
};

export const createVenta = async (ventaData) => {
    const response = await api.post('/ventas', ventaData);
    return response.data;
};

export default api;
