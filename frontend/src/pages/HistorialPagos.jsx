import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Wifi,
  ArrowLeft,
  Receipt,
  CalendarDays,
  CreditCard,
  CheckCircle2,
  Clock,
  LoaderCircle,
  History,
  User,
  Gauge,
  LogOut,
} from "lucide-react";
import api from "../services/api";
import "./Dashboard.css";
import "./HistorialPagos.css";

function HistorialPagos() {
  const navigate = useNavigate();

  const usuario = JSON.parse(localStorage.getItem("usuario") || "{}");

  const [pagos, setPagos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    cargarHistorial();
  }, []);

  const cargarHistorial = async () => {
    try {
      setCargando(true);
      setError("");

      const respuesta = await api.get("/pagos/mi-historial", {
        params: {
          page: 0,
          size: 100,
          sort: "fechaPago,desc",
        },
      });

      const datos = respuesta.data;
      setPagos(datos.content || []);
    } catch (err) {
      console.error("Error al consultar historial:", err);

      setError(
        err.response?.data?.message ||
          "No fue posible cargar tu historial de pagos."
      );
    } finally {
      setCargando(false);
    }
  };

  const formatoFecha = (fecha) => {
    if (!fecha) return "No disponible";

    return new Date(`${fecha}T00:00:00`).toLocaleDateString("es-MX", {
      day: "2-digit",
      month: "long",
      year: "numeric",
    });
  };

  const formatoMes = (mes) => {
    if (!mes) return "No disponible";

    const [anio, numeroMes] = mes.split("-");
    const fecha = new Date(Number(anio), Number(numeroMes) - 1, 1);

    return fecha.toLocaleDateString("es-MX", {
      month: "long",
      year: "numeric",
    });
  };

  const totalPagado = pagos.reduce(
    (total, pago) => total + Number(pago.monto || 0),
    0
  );

  const cerrarSesion = async () => {
    try {
      await api.post("/auth/logout");
    } catch (e) {
      /* si falla, igual cerramos */
    }
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    navigate("/login", { replace: true });
  };

  return (
    <main className="dashboard">      {/* ---------------- BARRA LATERAL ---------------- */}
      <aside className="dashboard-sidebar">
        <div className="dashboard-logo">
          <div className="dashboard-logo-icon">
            <Wifi size={25} />
          </div>
          <span>ENLÁSATE</span>
        </div>

        <div className="sidebar-section">
          <span className="sidebar-label">MENÚ PRINCIPAL</span>

          <button className="sidebar-link" onClick={() => navigate("/dashboard")}>
            <Gauge size={19} /> Inicio
          </button>

          <button className="sidebar-link active" onClick={() => navigate("/historial-pagos")}>
            <Receipt size={19} /> Historial de pagos
          </button>

          <button className="sidebar-link" onClick={() => navigate("/mi-estado-cuenta")}>
            <CalendarDays size={19} /> Mi estado de cuenta
          </button>
        </div>

        <div className="sidebar-bottom">
          <div className="sidebar-user">
            <div className="sidebar-avatar">
              <User size={20} />
            </div>
            <div>
              <strong>{usuario.nombre || "Cliente"}</strong>
              <span>Cliente</span>
            </div>
          </div>

          <button className="sidebar-logout" onClick={cerrarSesion}>
            <LogOut size={18} /> Cerrar sesión
          </button>
        </div>
      </aside>

      <section className="dashboard-main">
        <section className="historial-container">
          <button
            className="historial-back"
            onClick={() => navigate("/dashboard")}
          >
            <ArrowLeft size={18} />
            Volver al inicio
          </button>

        <div className="historial-heading">
          <div className="historial-heading-icon">
            <History size={28} />
          </div>

          <div>
            <span>MI CUENTA</span>
            <h1>Historial de pagos</h1>
            <p>
              Consulta tus pagos realizados y revisa los detalles de cada
              transacción.
            </p>
          </div>
        </div>

        <section className="historial-summary">
          <article className="historial-summary-card">
            <div className="summary-icon">
              <Receipt size={23} />
            </div>
            <div>
              <span>Pagos registrados</span>
              <strong>{pagos.length}</strong>
            </div>
          </article>

          <article className="historial-summary-card">
            <div className="summary-icon">
              <CreditCard size={23} />
            </div>
            <div>
              <span>Total pagado</span>
              <strong>
                ${totalPagado.toLocaleString("es-MX", {
                  minimumFractionDigits: 2,
                  maximumFractionDigits: 2,
                })}
              </strong>
            </div>
          </article>
        </section>

        <section className="historial-content">
          <div className="historial-content-heading">
            <div>
              <h2>Mis transacciones</h2>
              <p>Detalle de tus pagos registrados en ENLÁSATE.</p>
            </div>
          </div>

          {cargando ? (
            <div className="historial-loading">
              <LoaderCircle size={35} className="historial-spinner" />
              <p>Cargando historial de pagos...</p>
            </div>
          ) : error ? (
            <div className="historial-error">
              <p>{error}</p>
              <button onClick={cargarHistorial}>Intentar nuevamente</button>
            </div>
          ) : pagos.length === 0 ? (
            <div className="historial-empty">
              <Receipt size={45} />
              <h3>Aún no tienes pagos registrados</h3>
              <p>
                Cuando realices tu primer pago, aparecerá aquí toda la
                información de tu transacción.
              </p>
              <button onClick={() => navigate("/dashboard")}>
                Explorar planes
              </button>
            </div>
          ) : (
            <div className="historial-table-container">
              <table className="historial-table">
                <thead>
                  <tr>
                    <th>Pago</th>
                    <th>Plan</th>
                    <th>Mes correspondiente</th>
                    <th>Fecha de pago</th>
                    <th>Método</th>
                    <th>Monto</th>
                    <th>Estado</th>
                  </tr>
                </thead>

                <tbody>
                  {pagos.map((pago) => (
                    <tr key={pago.idPago}>
                      <td>
                        <strong>#{pago.idPago}</strong>
                      </td>

                      <td>
                        <div className="historial-plan">
                          <Wifi size={17} />
                          {pago.nombrePlan || "Plan no especificado"}
                        </div>
                      </td>

                      <td>
                        <CalendarDays size={15} />
                        {formatoMes(pago.mesCorrespondiente)}
                      </td>

                      <td>{formatoFecha(pago.fechaPago)}</td>

                      <td>
                        <span className="historial-method">
                          {pago.metodoPago}
                        </span>
                      </td>

                      <td>
                        <strong className="historial-amount">
                          ${Number(pago.monto).toFixed(2)}
                        </strong>
                      </td>

                      <td>
                        <span
                          className={`historial-status ${
                            pago.estadoPago === "Completado"
                              ? "status-completed"
                              : "status-pending"
                          }`}
                        >
                          {pago.estadoPago === "Completado" ? (
                            <CheckCircle2 size={15} />
                          ) : (
                            <Clock size={15} />
                          )}
                          {pago.estadoPago || "Sin estado"}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <footer className="historial-footer">
          <span>© 2026 ENLÁSATE. Todos los derechos reservados.</span>
          <span>Tu conexión, nuestra prioridad.</span>
        </footer>
      </section>
      </section>
    </main>
  );
}
export default HistorialPagos;