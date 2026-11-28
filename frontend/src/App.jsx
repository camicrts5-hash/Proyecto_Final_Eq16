import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import AdminPanel from "./pages/AdminPanel";
import PendienteInstalacion from "./pages/PendienteInstalacion";
import FichaCliente from "./pages/FichaCliente";
import RecuperarPassword from "./pages/RecuperarPassword";
import ReportarFalla from "./pages/ReportarFalla";
import OAuth2Redirect from "./pages/OAuth2Redirect";
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

        {/* REGRESO DEL LOGIN CON GOOGLE */}
        <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />

        {/* RECUPERACIÓN DE CONTRASEÑA */}
        <Route path="/recuperar" element={<RecuperarPassword />} />
        <Route path="/restablecer" element={<RecuperarPassword />} />

        {/* CUENTA CREADA PERO SIN INSTALAR */}
        <Route
          path="/pendiente"
          element={
            <RutaProtegida>
              <PendienteInstalacion />
            </RutaProtegida>
          }
        />

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
          path="/reportar-falla"
          element={
            <RutaProtegida rolPermitido="CLIENTE">
              <ReportarFalla />
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
              <AdminPanel />
            </RutaProtegida>
          }
        />

        <Route
          path="/admin/cliente/:id"
          element={
            <RutaProtegida rolPermitido="ADMIN">
              <FichaCliente />
            </RutaProtegida>
          }
        />

        <Route
          path="/empleado/cliente/:id"
          element={
            <RutaProtegida rolPermitido="EMPLEADO">
              <FichaCliente />
            </RutaProtegida>
          }
        />

        <Route
          path="/mi-estado-cuenta"
          element={
            <RutaProtegida rolPermitido="CLIENTE">
              <FichaCliente mi />
            </RutaProtegida>
          }
        />

        {/* PANEL DE EMPLEADO (mismo panel, el menú se filtra por rol) */}
        <Route
          path="/empleado"
          element={
            <RutaProtegida rolPermitido="EMPLEADO">
              <AdminPanel />
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