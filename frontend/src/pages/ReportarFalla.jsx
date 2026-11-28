import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Wifi, Gauge, Receipt, CalendarDays, LifeBuoy, LogOut, User,
  LoaderCircle, Send,
} from "lucide-react";
import api from "../services/api";
import "./Dashboard.css";
import "./ReportarFalla.css";

const COLORES = {
  Abierto: "chip abierto",
  "En proceso": "chip proceso",
  Cerrado: "chip cerrado",
};

function ReportarFalla() {
  const navigate = useNavigate();
  const usuario = JSON.parse(localStorage.getItem("usuario") || "{}");

  const [descripcion, setDescripcion] = useState("");
  const [tickets, setTickets] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState("");
  const [ok, setOk] = useState("");

  const cargar = () => {
    setCargando(true);
    api.get("/soporte/mis-tickets", { params: { page: 0, size: 20 } })
      .then(({ data }) => setTickets(data.content || []))
      .catch(() => setTickets([]))
      .finally(() => setCargando(false));
  };

  useEffect(() => { cargar(); }, []);

  const enviar = async (event) => {
    event.preventDefault();
    setError("");
    setOk("");

    if (descripcion.trim().length < 10) {
      setError("Describe la falla con un poco más de detalle (mínimo 10 caracteres).");
      return;
    }

    setEnviando(true);
    try {
      await api.post("/soporte/mi-ticket", { descripcion: descripcion.trim() });
      setDescripcion("");
      setOk("Reporte enviado. Un asesor te contactará pronto.");
      cargar();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo enviar el reporte.");
    } finally {
      setEnviando(false);
    }
  };

  const cerrarSesion = async () => {
    try { await api.post("/auth/logout"); } catch (e) {}
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    navigate("/login", { replace: true });
  };

  return (
    <main className="dashboard">
      <aside className="dashboard-sidebar">
        <div className="dashboard-logo">
          <div className="dashboard-logo-icon"><Wifi size={25} /></div>
          <span>ENLÁSATE</span>
        </div>

        <div className="sidebar-section">
          <span className="sidebar-label">MENÚ PRINCIPAL</span>

          <button className="sidebar-link" onClick={() => navigate("/dashboard")}>
            <Gauge size={19} /> Inicio
          </button>
          <button className="sidebar-link" onClick={() => navigate("/historial-pagos")}>
            <Receipt size={19} /> Historial de pagos
          </button>
          <button className="sidebar-link" onClick={() => navigate("/mi-estado-cuenta")}>
            <CalendarDays size={19} /> Mi estado de cuenta
          </button>
          <button className="sidebar-link active" onClick={() => navigate("/reportar-falla")}>
            <LifeBuoy size={19} /> Reportar falla
          </button>
        </div>

        <div className="sidebar-bottom">
          <div className="sidebar-user">
            <div className="sidebar-avatar"><User size={20} /></div>
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
        <header className="dashboard-header">
          <div>
            <span className="dashboard-date">SOPORTE TÉCNICO</span>
            <h1>Reportar una falla</h1>
            <p>Cuéntanos qué pasa y un asesor te contactará.</p>
          </div>

          <div className="dashboard-profile">
            <div className="profile-icon"><User size={21} /></div>
            <div>
              <strong>{usuario.nombre}</strong>
              <span>{usuario.email}</span>
            </div>
          </div>
        </header>

        <form className="falla-form" onSubmit={enviar}>
          <label>
            ¿Qué está fallando?
            <textarea
              rows={4}
              maxLength={255}
              placeholder="Ej. Desde ayer el módem no enciende y no tengo internet."
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
            />
          </label>

          <div className="falla-acciones">
            <span className="falla-contador">{descripcion.length}/255</span>
            <button type="submit" className="ficha-btn-cobrar" disabled={enviando}>
              <Send size={16} />
              {enviando ? "Enviando…" : "Enviar reporte"}
            </button>
          </div>

          {error && <div className="panel-error">{error}</div>}
          {ok && <div className="falla-ok">{ok}</div>}
        </form>

        <h2 className="falla-subtitulo">Mis reportes</h2>

        {cargando ? (
          <div className="panel-estado">
            <LoaderCircle className="girando" size={22} /> Cargando…
          </div>
        ) : tickets.length === 0 ? (
          <div className="panel-estado">Todavía no has reportado ninguna falla.</div>
        ) : (
          <div className="panel-tabla-scroll">
            <table className="panel-tabla">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Falla reportada</th>
                  <th>Fecha</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {tickets.map((t) => (
                  <tr key={t.idTicket}>
                    <td>{t.idTicket}</td>
                    <td>{t.descripcionFalla}</td>
                    <td>{String(t.fechaReporte || "").replace("T", " ").slice(0, 16)}</td>
                    <td>
                      <span className={COLORES[t.estadoTicket] || "chip"}>
                        {t.estadoTicket}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}

export default ReportarFalla;
