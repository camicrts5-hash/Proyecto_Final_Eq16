import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import MetodoPago from "./pages/MetodoPago";
import HistorialPagos from "./pages/HistorialPagos";

function obtenerRutaPorRol() {
  const usuario = JSON.parse(localStorage.getItem("usuario") || "{}");
  const rol = String(usuario.rol || "").toUpperCase();

  if (rol === "ADMIN") return "/admin";
  if (rol === "EMPLEADO") return "/empleado";

  return "/dashboard";
}

function RutaProtegida({ children, rolPermitido }) {
  const token = localStorage.getItem("token");
  const usuario = JSON.parse(localStorage.getItem("usuario") || "{}");
  const rol = String(usuario.rol || "").toUpperCase();

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (rolPermitido && rol !== rolPermitido) {
    return <Navigate to={obtenerRutaPorRol()} replace />;
  }

  return children;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={<Navigate to="/login" replace />}
        />

        <Route path="/login" element={<Login />} />
        <Route path="/registro" element={<Login />} />

        {/* PANEL DE CLIENTE */}
        <Route
          path="/dashboard"
          element={
            <RutaProtegida rolPermitido="CLIENTE">
              <Dashboard />
            </RutaProtegida>
          }
        />

        <Route
          path="/metodo-pago"
          element={
            <RutaProtegida rolPermitido="CLIENTE">
              <MetodoPago />
            </RutaProtegida>
          }
        />

        <Route
          path="/historial-pagos"
          element={
            <RutaProtegida rolPermitido="CLIENTE">
              <HistorialPagos />
            </RutaProtegida>
          }
        />

        {/* PANEL DE ADMINISTRADOR */}
        <Route
          path="/admin"
          element={
            <RutaProtegida rolPermitido="ADMIN">
              <h1>Panel de administrador</h1>
              <p>Bienvenido al sistema ENLÁSATE.</p>
            </RutaProtegida>
          }
        />

        {/* PANEL DE EMPLEADO */}
        <Route
          path="/empleado"
          element={
            <RutaProtegida rolPermitido="EMPLEADO">
              <h1>Panel de empleado</h1>
              <p>Bienvenido al sistema ENLÁSATE.</p>
            </RutaProtegida>
          }
        />

        <Route
          path="*"
          element={<Navigate to="/login" replace />}
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;