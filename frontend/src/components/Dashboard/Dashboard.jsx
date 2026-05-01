import { useState, useEffect } from 'react';
import { getProductos, getVentas, getInventario } from '../../services/api';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

export default function Dashboard() {
    const [stats, setStats] = useState({ ventas: 0, productos: 0, ingresos: 0 });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const [ventasRes, productosRes] = await Promise.all([
                    getVentas(),
                    getProductos()
                ]);

                let totalIngresos = 0;
                if (ventasRes.success) {
                    totalIngresos = ventasRes.data.reduce((sum, venta) => sum + Number(venta.Total), 0);
                }

                setStats({
                    ventas: ventasRes.success ? ventasRes.count : 0,
                    productos: productosRes.success ? productosRes.count : 0,
                    ingresos: totalIngresos
                });
            } catch (error) {
                console.error("Error fetching dashboard data", error);
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    if (loading) return <div className="loading-spinner"><div className="spinner"></div></div>;

    const dummyData = [
        { name: 'Lun', ventas: 4000 },
        { name: 'Mar', ventas: 3000 },
        { name: 'Mie', ventas: 2000 },
        { name: 'Jue', ventas: 2780 },
        { name: 'Vie', ventas: 1890 },
        { name: 'Sab', ventas: 2390 },
        { name: 'Dom', ventas: 3490 },
    ];

    return (
        <div className="dashboard-container">
            <h1 className="page-title">Dashboard Panadería</h1>
            <p className="page-subtitle">Resumen General de Operaciones</p>

            <div className="grid-container" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
                <div className="card">
                    <div className="card-header">
                        <h2 className="card-title">Ventas Totales</h2>
                    </div>
                    <div style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--color-primary)' }}>
                        {stats.ventas}
                    </div>
                </div>
                <div className="card">
                    <div className="card-header">
                        <h2 className="card-title">Ingresos Acumulados</h2>
                    </div>
                    <div style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--color-success)' }}>
                        ${stats.ingresos.toFixed(2)}
                    </div>
                </div>
                <div className="card">
                    <div className="card-header">
                        <h2 className="card-title">Productos Activos</h2>
                    </div>
                    <div style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--color-warning)' }}>
                        {stats.productos}
                    </div>
                </div>
            </div>

            <div className="card" style={{ marginTop: '2rem' }}>
                <div className="card-header">
                    <h2 className="card-title">Ventas de la Semana</h2>
                </div>
                <div style={{ width: '100%', height: 300 }}>
                    <ResponsiveContainer>
                        <BarChart data={dummyData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                            <XAxis dataKey="name" stroke="#94a3b8" />
                            <YAxis stroke="#94a3b8" />
                            <Tooltip contentStyle={{ backgroundColor: '#1a2332', borderColor: '#1e293b' }} />
                            <Legend />
                            <Bar dataKey="ventas" fill="#6366f1" radius={[4, 4, 0, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>
        </div>
    );
}
