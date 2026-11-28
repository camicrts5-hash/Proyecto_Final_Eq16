import { useEffect, useState, useCallback } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  Wifi, Gauge, Users, Radio, Receipt, LifeBuoy, UserCog,
  LogOut, User, CheckCircle2, Circle, MinusCircle, Wallet,
  ArrowLeft, LoaderCircle, KeyRound, CalendarDays, Clock, MapPin,
} from "lucide-react";
import api from "../services/api";
import CambiarPassword from "../components/CambiarPassword";
import "./Dashboard.css";
import "./FichaCliente.css";

const MESES_CORTOS = ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"];

function FichaCliente({ mi = false }) {
  const navigate = useNavigate();
  const { id } = useParams();
  const usuario = JSON.parse(localStorage.getItem("usuario") || "{}");
  const rol = String(usuario.rol || "").toUpperCase();
  const esAdmin = rol === "ADMIN";
  const esCliente = rol === "CLIENTE";
  const esStaff = rol === "ADMIN" || rol === "EMPLEADO";
  // El personal cobra cualquier método; el cliente paga su propia cuenta
  const puedeCobrar = esStaff || mi;

  const [datos, setDatos] = useState(null);
  const [domicilio, setDomicilio] = useState(null);
  const [anio, setAnio] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [cambiarPass, setCambiarPass] = useState(false);

  const [cobrando, setCobrando] = useState(false);
  const [seleccion, setSeleccion] = useState([]);
  const [metodo, setMetodo] = useState("Efectivo");
  const [monto, setMonto] = useState("");
  const [guardando, setGuardando] = useState(false);
  const [procesando, setProcesando] = useState(false);

  const suspender = async () => {
    if (!window.confirm(`¿Suspender el servicio de ${datos.nombre} desde hoy?`)) return;
    setProcesando(true);
    setError("");
    try {
      await api.post(`/clientes/${datos.idCliente}/suspender`, {
        motivo: "Baja solicitada por el cliente",
      });
      cargar();
    } catch (e) {
      setError(e?.response?.data?.message || "No se pudo suspender el servicio.");
    } finally {
      setProcesando(false);
    }
  };

  const reactivar = async () => {
    setProcesando(true);
    setError("");
    try {
      await api.post(`/clientes/${datos.idCliente}/reactivar`);
      cargar();
    } catch (e) {
      setError(e?.response?.data?.message || "No se pudo reactivar el servicio.");
    } finally {
      setProcesando(false);
    }
  };

  const confirmarPago = async (idPago) => {
    setProcesando(true);
    setError("");
    try {
      await api.post(`/pagos/${idPago}/confirmar`);
      cargar();
    } catch (e) {
      setError(e?.response?.data?.message || "No se pudo confirmar el pago.");
    } finally {
      setProcesando(false);
    }
  };

  const cargar = useCallback(async () => {
    setCargando(true);
    setError("");
    try {
      const url = mi ? "/pagos/mi-estado-cuenta" : `/pagos/estado-cuenta/${id}`;
      const { data } = await api.get(url, { params: anio ? { anio } : undefined });
      setDatos(data);
      if (!anio) setAnio(data.anio);
    } catch (e) {
      setError(e?.response?.data?.message || "No se pudo cargar el estado de cuenta.");
    } finally {
      setCargando(false);
    }
  }, [mi, id, anio]);

  useEffect(() => { cargar(); }, [cargar]);

  useEffect(() => {
    const idCliente = mi ? datos?.idCliente : id;

    if (!idCliente) return;

    api.get(`/domicilios/cliente/${idCliente}`)
      .then(({ data }) => setDomicilio(Array.isArray(data) && data.length > 0 ? data[0] : null))
      .catch(() => setDomicilio(null));
  }, [mi, id, datos?.idCliente]);

  const cerrarSesion = async () => {    try { await api.post("/auth/logout"); } catch (e) {}
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    navigate("/login", { replace: true });
  };

  const volver = () =>
    navigate(mi ? "/dashboard" : esAdmin ? "/admin" : "/empleado");

  const abrirCobro = () => {
    setCobrando(true);
    setSeleccion([]);
    setMetodo(esStaff ? "Efectivo" : "Tarjeta");
    setMonto(datos?.precioPlan != null ? String(datos.precioPlan) : "");
    setError("");
  };

  const alternarMes = (n) =>
    setSeleccion((s) => (s.includes(n) ? s.filter((x) => x !== n) : [...s, n]));

  const registrar = async () => {
    if (seleccion.length === 0 || (esStaff && !monto)) return;
    setGuardando(true);
    setError("");
    try {
      for (const mes of [...seleccion].sort((a, b) => a - b)) {        const periodo = `${anio}-${String(mes).padStart(2, "0")}`;

        if (esStaff) {
          // El personal cobra en nombre del cliente (cualquier método)
          await api.post("/pagos", {
            idCliente: datos.idCliente,
            monto: Number(monto),
            mesCorrespondiente: periodo,
            fechaPago: new Date().toISOString().slice(0, 10),
            metodoPago: metodo,
          });
        } else {
          // El cliente paga su propia cuenta
          await api.post("/pagos/mi-pago", {
            idPlan: datos.idPlan,
            metodoPago: metodo,
            mes: periodo,
          });
        }
      }
      setCobrando(false);
      cargar();
    } catch (e) {
      setError(
        e?.response?.data?.message ||
        e?.response?.data?.mensaje ||
        "No se pudo registrar el pago."
      );
    } finally {
      setGuardando(false);
    }
  };

  return (
    <main className="dashboard">
      {/* ---------------- BARRA LATERAL ---------------- */}
      <aside className="dashboard-sidebar">
        <div className="dashboard-logo">
          <div className="dashboard-logo-icon"><Wifi size={25} /></div>
          <span>ENLÁSATE</span>
        </div>

        <div className="sidebar-section">
          <span className="sidebar-label">MENÚ PRINCIPAL</span>

          {esCliente ? (
            <>
              <button className="sidebar-link" onClick={() => navigate("/dashboard")}>
                <Gauge size={19} /> Inicio
              </button>
              <button className="sidebar-link" onClick={() => navigate("/historial-pagos")}>
                <Receipt size={19} /> Historial de pagos
              </button>
              <button className="sidebar-link active" onClick={() => navigate("/mi-estado-cuenta")}>
                <CalendarDays size={19} /> Mi estado de cuenta
              </button>
              <button className="sidebar-link" onClick={() => navigate("/reportar-falla")}>
                <LifeBuoy size={19} /> Reportar falla
              </button>
            </>
          ) : (
            <>
              <button className="sidebar-link" onClick={volver}>
                <Users size={19} /> Clientes
              </button>
              <button className="sidebar-link" onClick={() => navigate(esAdmin ? "/admin" : "/empleado")}>
                <Radio size={19} /> Planes y Equipos
              </button>
              <button className="sidebar-link" onClick={() => navigate(esAdmin ? "/admin" : "/empleado")}>
                <Receipt size={19} /> Pagos y Facturación
              </button>
              <button className="sidebar-link" onClick={() => navigate(esAdmin ? "/admin" : "/empleado")}>
                <LifeBuoy size={19} /> Soporte Técnico
              </button>
              {esAdmin && (
                <button className="sidebar-link" onClick={() => navigate("/admin")}>
                  <UserCog size={19} /> Gestión de Usuarios
                </button>
              )}
            </>
          )}
        </div>

        <div className="sidebar-bottom">
          <div className="sidebar-user">
            <div className="sidebar-avatar"><User size={20} /></div>
            <div>
              <strong>{usuario.nombre || "Usuario"}</strong>
              <span>{esAdmin ? "Administrador" : rol === "EMPLEADO" ? "Empleado" : "Cliente"}</span>
            </div>
          </div>

          <button className="sidebar-logout" onClick={() => setCambiarPass(true)}>
            <KeyRound size={18} /> Cambiar contraseña
          </button>

          <button className="sidebar-logout" onClick={cerrarSesion}>
            <LogOut size={18} /> Cerrar sesión
          </button>
        </div>
      </aside>

      {/* ---------------- CONTENIDO ---------------- */}
      <section className="dashboard-main">
        <header className="dashboard-header">
          <div>
            <span className="dashboard-date">
              {mi ? "MI SERVICIO" : "FICHA DEL CLIENTE"}
            </span>
            <h1>{datos?.nombre || "Cargando…"}</h1>
            <p>{datos ? `${datos.email} · ${datos.telefono}` : ""}</p>
            <p className="ficha-direccion">
              <MapPin size={14} />
              {domicilio
                ? `${domicilio.calle} #${domicilio.numero}, Col. ${domicilio.colonia}, ${domicilio.nombreMunicipio || ""}, ${domicilio.nombreEstado || "Oaxaca"}, C.P. ${domicilio.codigoPostal}`
                : "Sin domicilio registrado"}
            </p>
          </div>

          <div className="dashboard-profile">
            <div className="profile-icon"><User size={21} /></div>
            <div>
              <strong>{usuario.nombre}</strong>
              <span>{usuario.email}</span>
            </div>
          </div>
        </header>

        {cargando && !datos && (
          <div className="panel-estado">
            <LoaderCircle className="girando" size={22} /> Cargando estado de cuenta…
          </div>
        )}

        {error && <div className="panel-error">{error}</div>}

        {datos && (
          <>
            <div className="ficha-datos">
              <div>
                <span>Plan</span>
                <strong>{datos.plan || "Sin plan"}</strong>
              </div>
              <div>
                <span>Estado</span>
                <strong>{datos.estado}</strong>
              </div>
              <div>
                <span>Asesor</span>
                <strong>{datos.empleado || "Sin asesor"}</strong>
              </div>
              <div>
                <span>Servicio desde</span>
                <strong>{datos.inicioServicio || datos.fechaAlta || "—"}</strong>
              </div>
              <div>
                <span>Alta</span>
                <strong>{datos.fechaAlta || "—"}</strong>
              </div>
            </div>

            <div className="ficha-barra">
              <div className="ficha-anios">
                {datos.aniosDisponibles.map((a) => (
                  <button
                    key={a}
                    className={"ficha-anio" + (a === anio ? " activo" : "")}
                    onClick={() => setAnio(a)}
                  >
                    {a}
                  </button>
                ))}
              </div>

              {puedeCobrar && (
                <div className="ficha-acciones">
                  <button className="ficha-btn-cobrar" onClick={abrirCobro}>
                    <Wallet size={17} /> {esStaff ? "Registrar pago" : "Pagar mis meses"}
                  </button>

                  {datos.servicioActivo ? (
                    <button
                      className="ficha-btn-suspender"
                      onClick={suspender}
                      disabled={procesando}
                    >
                      {procesando ? "Procesando…" : "Suspender servicio"}
                    </button>
                  ) : (
                    <button
                      className="ficha-btn-reactivar"
                      onClick={reactivar}
                      disabled={procesando}
                    >
                      {procesando ? "Procesando…" : "Reactivar servicio"}
                    </button>
                  )}
                </div>
              )}
            </div>

            <div className="ficha-totales">
              <div>
                <span>Meses pagados en {anio}</span>
                <strong>{datos.mesesPagados} de 12</strong>
              </div>
              {!mi && (
                <div>
                  <span>Total cobrado</span>
                  <strong>${Number(datos.totalPagado).toFixed(2)}</strong>
                </div>
              )}
              <div>
                <span>Meses pendientes</span>
                <strong>
                  {datos.meses.filter((m) => m.aplica && !m.pagado).length}
                </strong>
              </div>
            </div>

            <div className="ficha-meses">
              {datos.meses.map((m) => (
                <div
                  key={m.numero}
                  className={
                    "ficha-mes " +
                    (m.pagado
                      ? "pagado"
                      : m.enRevision
                        ? "revision"
                        : m.aplica
                          ? "pendiente"
                          : "sin-servicio")
                  }
                  title={
                    m.pagado
                      ? `${m.nombre}: pagado${m.fechaPago ? " el " + m.fechaPago : ""}`
                      : m.enRevision
                        ? `${m.nombre}: pago en revisión (${m.metodoPago})`
                        : m.aplica
                          ? `${m.nombre}: sin pagar`
                          : `${m.nombre}: sin servicio`
                  }
                >
                  <span className="ficha-mes-icono">
                    {m.pagado ? <CheckCircle2 size={16} />
                      : m.enRevision ? <Clock size={16} />
                      : m.aplica ? <Circle size={16} /> : <MinusCircle size={16} />}
                  </span>
                  <span className="ficha-mes-nombre">{MESES_CORTOS[m.numero - 1]}</span>
                  <span className="ficha-mes-estado">
                    {m.pagado
                      ? `$${Number(m.monto).toFixed(0)}`
                      : m.enRevision
                        ? "revisión"
                        : m.aplica ? "—" : "n/a"}
                  </span>

                  {m.enRevision && puedeCobrar && (
                    <button
                      className="ficha-confirmar"
                      onClick={() => confirmarPago(m.idPago)}
                      disabled={procesando}
                      title="Confirmar este pago"
                    >
                      Confirmar
                    </button>
                  )}
                </div>
              ))}
            </div>

            <div className="ficha-leyenda">
              <span><i className="punto verde" /> Pagado</span>
              <span><i className="punto amarillo" /> En revisión</span>
              <span><i className="punto rojo" /> Sin pagar</span>
              <span><i className="punto gris" /> Sin servicio</span>
            </div>

            <button className="ficha-volver" onClick={volver}>
              <ArrowLeft size={17} /> Volver
            </button>
          </>
        )}
      </section>

      {/* ---------------- modal de cobro ---------------- */}
      {cobrando && (
        <div className="modal-fondo" onClick={() => setCobrando(false)}>
          <div className="modal-caja ficha-modal" onClick={(e) => e.stopPropagation()}>
            <h3 className="modal-titulo">Registrar pago de {anio}</h3>
            <p className="modal-mensaje">
              Selecciona los meses que está pagando. Puedes elegir varios.
            </p>

            {!esStaff && String(datos.estado).toLowerCase() === "pendiente" && (
              <p className="ficha-nota-pago error">
                Tu cuenta todavía no está activa. Primero actívala con el código
                que te dé tu asesor y después podrás pagar.
              </p>
            )}

            {!esStaff && !datos.idPlan && (
              <p className="ficha-nota-pago error">
                Todavía no tienes un plan asignado. Elige uno en el inicio para
                poder pagar tus meses.
              </p>
            )}

            <div className="ficha-sel-meses">
              {datos.meses.map((m) => {
                const bloqueado = m.pagado || !m.aplica || m.enRevision;

                return (
                  <button
                    key={m.numero}
                    type="button"
                    disabled={bloqueado}
                    className={
                      "ficha-pick" +
                      (seleccion.includes(m.numero) ? " sel" : "") +
                      (m.pagado ? " ya" : "") +
                      (!m.aplica ? " na" : "") +
                      (m.enRevision ? " ya" : "")
                    }
                    onClick={() => alternarMes(m.numero)}
                  >
                    {MESES_CORTOS[m.numero - 1]}
                  </button>
                );
              })}
            </div>

            {esStaff && (
              <label className="ficha-campo">
                Monto por mes
                <input
                  type="number"
                  step="0.01"
                  min="1"
                  value={monto}
                  onChange={(e) => setMonto(e.target.value)}
                />
              </label>
            )}

            <label className="ficha-campo">
              Método
              <select value={metodo} onChange={(e) => setMetodo(e.target.value)}>
                {esStaff ? (
                  <>
                    <option>Efectivo</option>
                    <option>Deposito</option>
                    <option>Transferencia</option>
                    <option>Tarjeta</option>
                  </>
                ) : (
                  <>
                    <option>Tarjeta</option>
                    <option>Transferencia</option>
                  </>
                )}
              </select>
            </label>

            <p className="ficha-resumen-pago">
              {seleccion.length} mes(es) ·{" "}
              {esStaff ? (
                <>total <strong>${(Number(monto || 0) * seleccion.length).toFixed(2)}</strong></>
              ) : datos.precioPlan != null ? (
                <>total <strong>${(Number(datos.precioPlan) * seleccion.length).toFixed(2)}</strong></>
              ) : (
                <>total <strong>—</strong></>
              )}
            </p>

            {!esStaff && metodo === "Transferencia" && (
              <p className="ficha-nota-pago">
                Tu pago por transferencia quedará <b>en revisión</b> hasta que un
                asesor lo confirme.
              </p>
            )}

            <div className="modal-acciones">
              <button className="modal-btn secundario" onClick={() => setCobrando(false)}>
                Cancelar
              </button>
              <button
                className="modal-btn peligro"
                onClick={registrar}
                disabled={
                  seleccion.length === 0 ||
                  (esStaff && !monto) ||
                  (!esStaff &&
                    (String(datos.estado).toLowerCase() === "pendiente" ||
                      !datos.idPlan)) ||
                  guardando
                }
              >
                {guardando ? "Guardando…" : esStaff ? "Registrar pago" : "Pagar ahora"}
              </button>
            </div>
          </div>
        </div>
      )}

      <CambiarPassword abierto={cambiarPass} onCerrar={() => setCambiarPass(false)} />
    </main>
  );
}

export default FichaCliente;
