import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Dashboard from './components/Dashboard/Dashboard';
import './index.css';

export default function App() {
  return (
    <BrowserRouter>
      <div className="app-layout" style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
        <header style={{ 
          height: '64px', 
          background: 'var(--color-bg-sidebar)', 
          borderBottom: '1px solid var(--color-border)',
          display: 'flex',
          alignItems: 'center',
          padding: '0 32px'
        }}>
          <div style={{
            fontSize: '1.25rem',
            fontWeight: '700',
            background: 'var(--gradient-primary)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent'
          }}>
            Panadería Management System
          </div>
        </header>
        <main className="app-main page-container">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            {/* Future routes for POS, Inventory, Login */}
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}
