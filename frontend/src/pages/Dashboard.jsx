import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Receipt,
  Wifi,
  LogOut,
  User,
  Gauge,
  CheckCircle2,
  ArrowRight,
  LoaderCircle,
  Radio,
  ShieldCheck,
  KeyRound,
  CalendarDays,
  LifeBuoy,
} from "lucide-react";
import api from "../services/api";
import CambiarPassword from "../components/CambiarPassword";
import "./Dashboard.css";

function Dashboard() {
  const navigate = useNavigate();

  const usuario = JSON.parse(localStorage.getItem("usuario") || "{}");

  const [planes, setPlanes] = useState([]);
  const [planSeleccionado, setPlanSeleccionado] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [cambiarPass, setCambiarPass] = useState(false);
  const [cliente, setCliente] = useState(null);

  useEffect(() => {
    if (!localStorage.getItem("token")) {
      navigate("/login");
      return;
    }

    cargarPlanes();
  }, [navigate]);

  const cargarPlanes = async () => {
    setCargando(true);
    setError("");

    // 1. Planes: se intentan siempre, sin depender de lo demás
    let planesDisponibles = [];

    try {
      const { data } = await api.get("/planes");
      planesDisponibles = Array.isArray(data) ? data : [];
      setPlanes(planesDisponibles);
    } catch (err) {
      setPlanes([]);
    }

    // 2. Datos del cliente: si no tiene ficha, igual puede elegir plan
    try {
      const { data } = await api.get("/clientes/mi-plan");
      setCliente(data);

      if (data?.plan) {
        const planActual = planesDisponibles.find(
          (plan) => plan.nombrePlan === data.plan
        );
        setPlanSeleccionado(planActual ? planActual.idPlan : null);
      } else {
        setPlanSeleccionado(null);
      }
    } catch (err) {
      setCliente(null);
      setPlanSeleccionado(null);
    } finally {
      setCargando(false);
    }
  };

  // Enviar al usuario a la pantalla de pago con el plan elegido
  const seleccionarPlan = async (plan) => {
    // ¿Todavía necesita activarse o elegir asesor?
    // Solo necesita activarse si sigue Pendiente. El asesor es opcional.
    const faltaActivar = !cliente || cliente.estado === "Pendiente";

    // El cliente NO puede cambiarse el plan: solo contacta a su asesor.
    // El cambio lo hace el empleado o el administrador.
    navigate(faltaActivar ? "/pendiente" : "/pendiente?cambio=1");
  };

  const cerrarSesion = async () => {
    try {
      await api.post("/auth/logout");
    } catch (err) {
      console.error("Error al cerrar sesión:", err);
    } finally {
      localStorage.removeItem("token");
      localStorage.removeItem("usuario");
      navigate("/login");
    }
  };

  return (
    <main className="dashboard">
      <aside className="dashboard-sidebar">
        <div className="dashboard-logo">
          <div className="dashboard-logo-icon">
            <Wifi size={25} />
          </div>

          <span>ENLÁSATE</span>
        </div>

        <div className="sidebar-section">
          <span className="sidebar-label">MENÚ PRINCIPAL</span>

          <a href="#inicio" className="sidebar-link active">
            <Gauge size={19} />
            Inicio
          </a>

          <a href="#planes" className="sidebar-link">
            <Radio size={19} />
            Planes de internet
          </a> 
          <button
  className="sidebar-link"
  onClick={() => navigate("/historial-pagos")}
>
  <Receipt size={19} />
  Historial de pagos
</button>

          <button
            className="sidebar-link"
            onClick={() => navigate("/mi-estado-cuenta")}
          >
            <CalendarDays size={19} />
            Mi estado de cuenta
          </button>

          <button
            className="sidebar-link"
            onClick={() => navigate("/reportar-falla")}
          >
            <LifeBuoy size={19} />
            Reportar falla
          </button>
        </div>

        <div className="sidebar-bottom">
          <div className="sidebar-user">
            <div className="sidebar-avatar">
              <User size={20} />
            </div>

            <div>
              <strong>{usuario.nombre || "Usuario"}</strong>
              <span>Cliente</span>
            </div>
          </div>

          <button className="sidebar-logout" onClick={cerrarSesion}>
            <LogOut size={18} />
            Cerrar sesión
          </button>

          <button
            className="sidebar-logout"
            onClick={() => setCambiarPass(true)}
            style={{ marginTop: "6px" }}
          >
            <KeyRound size={18} />
            Cambiar contraseña
          </button>

          <CambiarPassword
            abierto={cambiarPass}
            onCerrar={() => setCambiarPass(false)}
          />
        </div>
      </aside>

      <section className="dashboard-main" id="inicio">
        <header className="dashboard-header">
          <div>
            <span className="dashboard-date">
              PANEL DE CLIENTE
            </span>

            <h1>Hola, {usuario.nombre || "bienvenido"}.</h1>

            <p>
              Administra tu cuenta y encuentra el plan ideal para ti.
            </p>
          </div>

          <div className="dashboard-profile">
            <div className="profile-icon">
              <User size={21} />
            </div>

            <div>
              <strong>{usuario.nombre || "Usuario"}</strong>
              <span>{usuario.email}</span>
            </div>
          </div>
        </header>

        <section className="dashboard-banner">
          <div className="banner-content">
            <span className="banner-tag">
              <Radio size={15} />
              TU CONEXIÓN COMIENZA AQUÍ
            </span>

            <h2>Conéctate con lo que más importa.</h2>

            <p>
              Descubre nuestros planes de internet y elige el que mejor
              se adapte a tus necesidades.
            </p>

            <a href="#planes" className="banner-button">
              EXPLORAR PLANES
              <ArrowRight size={18} />
            </a>
          </div>

          <div className="banner-decoration">
            <div className="banner-circle banner-circle-one">
              <Wifi size={75} />
            </div>

            <div className="banner-circle banner-circle-two"></div>
          </div>
        </section>

        <section className="dashboard-plans" id="planes">
          <div className="plans-heading">
            <div>
              <span className="section-eyebrow">
                CONOCE NUESTRAS OPCIONES
              </span>

              <h2>Planes de internet</h2>

              <p>
                Selecciona el plan que mejor se adapte a ti.
              </p>
            </div>

            <div className="plans-count">
              <Wifi size={18} />
              {planes.length} planes disponibles
            </div>
          </div>

          {error && (
            <div className="dashboard-alert error-alert">
              {error}
            </div>
          )}

          {cargando ? (
            <div className="plans-loading">
              <LoaderCircle className="loading-icon" size={35} />
              <p>Cargando planes...</p>
            </div>
          ) : planes.length === 0 ? (
            <div className="plans-empty">
              <Wifi size={38} />
              <h3>No hay planes disponibles</h3>
              <p>Por el momento no encontramos planes registrados.</p>
            </div>
          ) : (
            <div className="plans-grid">
              {planes.map((plan, index) => (
                <article
                  className={`plan-card ${
                    planSeleccionado === plan.idPlan
                      ? "selected-plan"
                      : ""
                  }`}
                  key={plan.idPlan}
                >
                  <div className="plan-card-top">
                    <div className="plan-icon">
                      <Wifi size={24} />
                    </div>

                    {planSeleccionado === plan.idPlan && (
                      <span className="selected-badge">
                        <CheckCircle2 size={14} />
                        Seleccionado
                      </span>
                    )}
                  </div>

                  <span className="plan-number">
                    PLAN 0{index + 1}
                  </span>

                  <h3>{plan.nombrePlan}</h3>

                  <div className="plan-speed">
                    <strong>{plan.velocidadMb}</strong>
                    <span>Mbps</span>
                  </div>

                  <p className="plan-speed-description">
                    Velocidad de conexión
                  </p>

                  <div className="plan-divider"></div>

                  <div className="plan-price">
                    <strong>
                      ${Number(plan.precioMensual).toFixed(2)}
                    </strong>

                    <span>MXN / mes</span>
                  </div>

                  <button
                    className="plan-button"
                    disabled={planSeleccionado === plan.idPlan}
                    onClick={() => seleccionarPlan(plan)}
                  >
                    {planSeleccionado === plan.idPlan ? (
                      <>
                        <CheckCircle2 size={18} />
                        PLAN SELECCIONADO
                      </>
                    ) : (
                      <>
                        ELEGIR ESTE PLAN
                        <ArrowRight size={18} />
                      </>
                    )}
                  </button>
                </article>
              ))}
            </div>
          )}
        </section>

        <footer className="dashboard-footer">
          <div>
            <ShieldCheck size={17} />
            Tu conexión, nuestra prioridad.
          </div>

          <span>© 2026 ENLÁSATE. Todos los derechos reservados.</span>
        </footer>
      </section>
    </main>
  );
}

export default Dashboard;