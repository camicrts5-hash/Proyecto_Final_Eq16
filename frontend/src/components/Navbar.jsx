import { useNavigate } from 'react-router-dom';
import api from '../services/api';

export default function Navbar({ titulo }) {
  const navigate = useNavigate();
  const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
  const inicial = (usuario.nombre || '?').charAt(0).toUpperCase();

  const cerrarSesion = async () => {
    try {
      await api.post('/auth/logout');
    } catch (e) {
      // si falla, igual cerramos localmente
    }
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    navigate('/login', { replace: true });
  };

  return (
    <header className="navbar">
      <div className="navbar-marca">
        <span className="navbar-logo">EN</span>
        <span className="navbar-titulo">{titulo || 'ENLASATE'}</span>
      </div>

      <div className="navbar-usuario">
        <span className="navbar-avatar">{inicial}</span>
        <div className="navbar-datos">
          <strong>{usuario.nombre || 'Usuario'}</strong>
          <small>{usuario.email}</small>
        </div>
        <button className="navbar-salir" onClick={cerrarSesion}>
          Salir
        </button>
      </div>
    </header>
  );
}