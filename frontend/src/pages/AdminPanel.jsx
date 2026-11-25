import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import {
  Wifi, Gauge, Users, Radio, Receipt, LifeBuoy, UserCog,
  LogOut, User, LoaderCircle, Search, Trash2, Network,
  UserPlus, KeyRound, CheckCircle2, Wallet, X, Pencil, Plus,
} from "lucide-react";
import api from "../services/api";
import ModalConfirmacion from "../components/ModalConfirmacion";
import CambiarPassword from "../components/CambiarPassword";
import "./Dashboard.css";
import "./AdminPanel.css";
import "./AdminExtras.css";

const TAM = 10;

const fechaCorta = (iso) => (iso ? String(iso).replace("T", " ").slice(0, 16) : "—");
const mesActual = () => new Date().toISOString().slice(0, 7);

function AdminPanel() {
  const navigate = useNavigate();
  const usuario = JSON.parse(localStorage.getItem("usuario") || "{}");
  const rol = String(usuario.rol || "").toUpperCase();
  const esAdmin = rol === "ADMIN";

  const [seccion, setSeccion] = useState("clientes");

  // ---------- clientes ----------
  const [clientes, setClientes] = useState([]);
  const [pagina, setPagina] = useState(0);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const [total, setTotal] = useState(0);
  const [texto, setTexto] = useState("");
  const [busqueda, setBusqueda] = useState("");
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState("");
  const [porBorrar, setPorBorrar] = useState(null);
  const [borrando, setBorrando] = useState(false);

  // ---------- alta manual ----------
  const [nuevo, setNuevo] = useState(false);
  const [form, setForm] = useState({ nombre: "", apellido: "", email: "", telefono: "", conCuenta: true, idPlan: "" });
  const [creando, setCreando] = useState(false);
  const [creado, setCreado] = useState(null);

  // ---------- cobro ----------
  const [cobrar, setCobrar] = useState(null);
  const [cobro, setCobro] = useState({ monto: "", mes: mesActual(), metodo: "Efectivo" });
  const [cobrando, setCobrando] = useState(false);

  // ---------- planes / pagos ----------
  const [planes, setPlanes] = useState([]);
  const [pagos, setPagos] = useState([]);
  const [paginaPagos, setPaginaPagos] = useState(0);
  const [totalPaginasPagos, setTotalPaginasPagos] = useState(0);

  // ---------- empleados ----------
  const [empleados, setEmpleados] = useState([]);
  const [nuevoEmp, setNuevoEmp] = useState(false);
  const [formEmp, setFormEmp] = useState({
    nombre: "", apellido: "", email: "", telefono: "", puesto: "",
  });
  const [creandoEmp, setCreandoEmp] = useState(false);
  const [porBorrarEmp, setPorBorrarEmp] = useState(null);
  const [borrandoEmp, setBorrandoEmp] = useState(false);
  const [cambiarPass, setCambiarPass] = useState(false);

  // ---------- reporte PDF ----------
  const [anioRep, setAnioRep] = useState(String(new Date().getFullYear()));
  const [mesRep, setMesRep] = useState("");
  const [bajandoPdf, setBajandoPdf] = useState(false);

  // ---------- edición ----------
  const [editCliente, setEditCliente] = useState(null);
  const [formEditC, setFormEditC] = useState({});
  const [guardandoEditC, setGuardandoEditC] = useState(false);

  const [editEmp, setEditEmp] = useState(null);
  const [formEditE, setFormEditE] = useState({});
  const [guardandoEditE, setGuardandoEditE] = useState(false);
  const [creandoCuenta, setCreandoCuenta] = useState(false);

  // ---------- restablecer contraseña de cliente ----------
  const [resetCliente, setResetCliente] = useState(null);
  const [reseteando, setReseteando] = useState(false);

  // ---------- planes ----------
  const [nuevoPlan, setNuevoPlan] = useState(false);
  const [formPlan, setFormPlan] = useState({ nombrePlan: "", velocidadMb: "", precioMensual: "" });
  const [editPlan, setEditPlan] = useState(null);
  const [guardandoPlan, setGuardandoPlan] = useState(false);
  const [porBorrarPlan, setPorBorrarPlan] = useState(null);
  const [borrandoPlan, setBorrandoPlan] = useState(false);

  // ---------- soporte ----------
  const [tickets, setTickets] = useState([]);
  const [paginaTickets, setPaginaTickets] = useState(0);
  const [totalPaginasTickets, setTotalPaginasTickets] = useState(0);
  const [cargandoTickets, setCargandoTickets] = useState(false);

  const cargarTickets = useCallback(() => {
    setCargandoTickets(true);
    api.get("/soporte", { params: { page: paginaTickets, size: TAM } })
      .then(({ data }) => {
        setTickets(data.content || []);
        setTotalPaginasTickets(data.totalPages || 0);
      })
      .catch(() => setTickets([]))
      .finally(() => setCargandoTickets(false));
  }, [paginaTickets]);

  const cambiarEstadoTicket = async (t, nuevoEstado) => {
    setError("");
    try {
      await api.put(`/soporte/${t.idTicket}`, {
        idCliente: t.idCliente,
        idEmpleado: t.idEmpleado || 1,
        descripcionFalla: t.descripcionFalla,
        fechaReporte: t.fechaReporte,
        estadoTicket: nuevoEstado,
        fechaCierre: nuevoEstado === "Cerrado" ? new Date().toISOString() : null,
      });
      cargarTickets();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo cambiar el estado del ticket.");
    }
  };

  const cargarPlanes = () =>
    api.get("/planes").then(({ data }) => setPlanes(data)).catch(() => setPlanes([]));

  const crearPlan = async (event) => {
    event.preventDefault();
    setGuardandoPlan(true);
    setError("");
    try {
      await api.post("/planes", {
        nombrePlan: formPlan.nombrePlan,
        velocidadMb: Number(formPlan.velocidadMb),
        precioMensual: Number(formPlan.precioMensual),
      });
      setFormPlan({ nombrePlan: "", velocidadMb: "", precioMensual: "" });
      setNuevoPlan(false);
      cargarPlanes();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo crear el plan.");
    } finally {
      setGuardandoPlan(false);
    }
  };

  const guardarEditPlan = async (event) => {
    event.preventDefault();
    setGuardandoPlan(true);
    setError("");
    try {
      await api.put(`/planes/${editPlan.idPlan}`, {
        nombrePlan: formPlan.nombrePlan,
        velocidadMb: Number(formPlan.velocidadMb),
        precioMensual: Number(formPlan.precioMensual),
      });
      setEditPlan(null);
      cargarPlanes();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo guardar el plan.");
    } finally {
      setGuardandoPlan(false);
    }
  };

  const borrarPlan = async () => {
    if (!porBorrarPlan) return;
    setBorrandoPlan(true);
    setError("");
    try {
      await api.delete(`/planes/${porBorrarPlan.idPlan}`);
      setPorBorrarPlan(null);
      cargarPlanes();
    } catch (e) {
      const d = e?.response?.data;
      setError(
        d?.message || d?.mensaje ||
        "No se pudo eliminar el plan. Puede tener clientes asignados."
      );
      setPorBorrarPlan(null);
    } finally {
      setBorrandoPlan(false);
    }
  };

  // debounce del buscador
  useEffect(() => {
    const t = setTimeout(() => {
      setBusqueda(texto.trim());
      setPagina(0);
    }, 400);
    return () => clearTimeout(t);
  }, [texto]);

  // al cambiar de pestaña se limpian los mensajes
  useEffect(() => {
    setError("");
  }, [seccion]);

  const cargarClientes = useCallback(async () => {
    setCargando(true);
    setError("");
    try {
      const { data } = await api.get("/clientes", {
        params: { page: pagina, size: TAM, q: busqueda || undefined },
      });
      setClientes(data.content || []);
      setTotalPaginas(data.totalPages || 0);
      setTotal(data.totalElements || 0);
    } catch (e) {
      setError("No se pudo cargar la lista de clientes.");
      setClientes([]);
    } finally {
      setCargando(false);
    }
  }, [pagina, busqueda]);

  useEffect(() => {
    if (seccion === "clientes") cargarClientes();
  }, [seccion, cargarClientes]);

  useEffect(() => {
    if (seccion === "planes" && planes.length === 0) {
      api.get("/planes").then(({ data }) => setPlanes(data)).catch(() => setPlanes([]));
    }
    if (seccion === "pagos") {
      api.get("/pagos", { params: { page: paginaPagos, size: TAM } })
        .then(({ data }) => {
          setPagos(data.content || []);
          setTotalPaginasPagos(data.totalPages || 0);
        })
        .catch(() => setPagos([]));
    }
  }, [seccion, paginaPagos, planes.length]);

  // ---------------- edición de cliente ----------------
  const abrirEdicionCliente = (c) => {
    setError("");
    setEditCliente(c);
    const plan = planes.find((p) => p.nombrePlan === c.plan);
    setFormEditC({
      nombre: c.nombre || "",
      apellido: "",
      email: c.email || "",
      telefono: c.telefono || "",
      idPlan: plan ? plan.idPlan : "",
      estado: c.estado || "Activo",
    });
    if (planes.length === 0) {
      api.get("/planes").then(({ data }) => setPlanes(data)).catch(() => {});
    }
  };

  const guardarCliente = async (event) => {
    event.preventDefault();
    setGuardandoEditC(true);
    setError("");
    try {
      await api.put(`/clientes/${editCliente.idCliente}`, {
        nombre: formEditC.nombre,
        apellido: formEditC.apellido,
        email: formEditC.email,
        telefono: formEditC.telefono,
        idPlan: Number(formEditC.idPlan),
        estado: formEditC.estado,
      });
      setEditCliente(null);
      cargarClientes();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo guardar el cliente.");
    } finally {
      setGuardandoEditC(false);
    }
  };

  const hacerResetPassword = async () => {
    if (!resetCliente) return;
    setReseteando(true);
    setError("");
    try {
      await api.post(`/clientes/${resetCliente.idCliente}/reset-password`);
      setResetCliente(null);
      setError("");
      alert(
        `Contraseña de ${resetCliente.nombre} restablecida a Enlasate2026!`
      );
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo restablecer la contraseña.");
      setResetCliente(null);
    } finally {
      setReseteando(false);
    }
  };

  // ---------------- crear cuenta web a un cliente sin cuenta ----------------
  const crearCuentaWeb = async () => {    if (!formEditC.idPlan) {
      setError("Primero selecciona un plan para el cliente.");
      return;
    }

    setCreandoCuenta(true);
    setError("");

    try {
      // Guarda los datos actuales (correo, teléfono, plan…)
      await api.put(`/clientes/${editCliente.idCliente}`, {
        nombre: formEditC.nombre,
        apellido: formEditC.apellido,
        email: formEditC.email,
        telefono: formEditC.telefono,
        idPlan: Number(formEditC.idPlan),
        estado: formEditC.estado,
      });

      // Crea la cuenta y devuelve el código
      const { data } = await api.post(
        `/clientes/${editCliente.idCliente}/crear-cuenta`
      );

      setEditCliente(null);
      setCreado(data);
      cargarClientes();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo crear la cuenta.");
    } finally {
      setCreandoCuenta(false);
    }
  };

  // ---------------- edición de empleado ----------------
  const abrirEdicionEmp = (emp) => {    setError("");
    setEditEmp(emp);
    setFormEditE({
      nombre: emp.nombre || "",
      apellido: "",
      email: emp.email || "",
      telefono: emp.telefono || "",
      puesto: emp.puesto || "",
    });
  };

  const guardarEmpleado = async (event) => {
    event.preventDefault();
    setGuardandoEditE(true);
    setError("");
    try {
      await api.put(`/empleados/${editEmp.idEmpleado}`, {
        nombre: formEditE.nombre,
        apellido: formEditE.apellido,
        email: formEditE.email,
        telefono: formEditE.telefono,
        puesto: formEditE.puesto,
      });
      setEditEmp(null);
      cargarEmpleados();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo guardar el empleado.");
    } finally {
      setGuardandoEditE(false);
    }
  };

  const descargarReporte = async () => {
    setBajandoPdf(true);
    setError("");
    try {
      const { data } = await api.get("/reportes/ventas.pdf", {
        params: {
          anio: Number(anioRep),
          mes: mesRep ? Number(mesRep) : undefined,
        },
        responseType: "blob",
      });

      const url = URL.createObjectURL(new Blob([data], { type: "application/pdf" }));
      const enlace = document.createElement("a");
      enlace.href = url;
      enlace.download = `reporte-ventas-${anioRep}${mesRep ? "-" + mesRep : ""}.pdf`;
      document.body.appendChild(enlace);
      enlace.click();
      document.body.removeChild(enlace);
      URL.revokeObjectURL(url);
    } catch (e) {
      setError("No se pudo generar el reporte. Intenta de nuevo.");
    } finally {
      setBajandoPdf(false);
    }
  };

  const cargarEmpleados = useCallback(() => {    api.get("/empleados")
      .then(({ data }) => setEmpleados(data))
      .catch(() => setEmpleados([]));
  }, []);

  useEffect(() => {
    if (seccion === "usuarios") cargarEmpleados();
    if (seccion === "soporte") cargarTickets();
  }, [seccion, cargarEmpleados, cargarTickets]);

  const crearEmpleado = async (event) => {
    event.preventDefault();
    setError("");
    setCreandoEmp(true);
    try {
      await api.post("/empleados", formEmp);
      setFormEmp({ nombre: "", apellido: "", email: "", telefono: "", puesto: "" });
      setNuevoEmp(false);
      cargarEmpleados();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo dar de alta al empleado.");
    } finally {
      setCreandoEmp(false);
    }
  };

  const borrarEmpleado = async () => {
    if (!porBorrarEmp) return;
    setBorrandoEmp(true);
    try {
      await api.delete(`/empleados/${porBorrarEmp.idEmpleado}`);
      setPorBorrarEmp(null);
      cargarEmpleados();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo dar de baja al empleado.");
      setPorBorrarEmp(null);
    } finally {
      setBorrandoEmp(false);
    }
  };

  // ---------------------------------------------------------------
  // Alta manual de cliente
  // ---------------------------------------------------------------
  const crearCliente = async (event) => {
    event.preventDefault();
    setError("");
    setCreando(true);
    try {
      const { data } = await api.post("/clientes", {
        ...form,
        idPlan: form.idPlan ? Number(form.idPlan) : null,
      });
      setCreado(data);
      setForm({ nombre: "", apellido: "", email: "", telefono: "", conCuenta: true, idPlan: "" });
      setNuevo(false);
      cargarClientes();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo dar de alta al cliente.");
    } finally {
      setCreando(false);
    }
  };

  // ---------------------------------------------------------------
  // Activar cliente pendiente
  // ---------------------------------------------------------------
  const activarCliente = async (cliente) => {
    setError("");
    try {
      await api.post(`/clientes/${cliente.idCliente}/activar`, {
        codigo: cliente.codigoActivacion || "ACTIVADO",
      });
      cargarClientes();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo activar el cliente.");
    }
  };

  const regenerarCodigo = async (cliente) => {
    setError("");
    try {
      const { data } = await api.post(`/clientes/${cliente.idCliente}/codigo`);
      setCreado(data);
      cargarClientes();
    } catch (e) {
      setError("No se pudo generar el código.");
    }
  };

  // ---------------------------------------------------------------
  // Cobro en efectivo / depósito
  // ---------------------------------------------------------------
  const abrirCobro = (cliente) => {
    setCobrar(cliente);
    setCobro({
      monto: cliente.precioPlan != null ? String(cliente.precioPlan) : "",
      mes: mesActual(),
      metodo: "Efectivo",
    });
  };

  const registrarCobro = async (event) => {
    event.preventDefault();
    if (!cobrar) return;
    setError("");
    setCobrando(true);
    try {
      await api.post("/pagos", {
        idCliente: cobrar.idCliente,
        monto: Number(cobro.monto),
        mesCorrespondiente: cobro.mes,
        fechaPago: new Date().toISOString().slice(0, 10),
        metodoPago: cobro.metodo,
      });
      setCobrar(null);
      cargarClientes();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo registrar el pago.");
    } finally {
      setCobrando(false);
    }
  };

  const confirmarBorrado = async () => {
    if (!porBorrar) return;
    setBorrando(true);
    try {
      await api.delete(`/clientes/${porBorrar.idCliente}`);
      const eraElUltimo = clientes.length === 1 && pagina > 0;
      setPorBorrar(null);
      if (eraElUltimo) setPagina(pagina - 1);
      else cargarClientes();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo eliminar el cliente.");
      setPorBorrar(null);
    } finally {
      setBorrando(false);
    }
  };

  const cerrarSesion = async () => {
    try { await api.post("/auth/logout"); } catch (e) {}
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    navigate("/login", { replace: true });
  };

  const menu = [
    { id: "clientes", label: "Clientes",            Icono: Users,     admin: false },
    { id: "planes",   label: "Planes y Equipos",    Icono: Radio,     admin: false },
    { id: "pagos",    label: "Pagos y Facturación", Icono: Receipt,   admin: false },
    { id: "soporte",  label: "Soporte Técnico",     Icono: LifeBuoy,  admin: false },
    { id: "usuarios", label: "Gestión de Usuarios", Icono: UserCog,   admin: true  },
  ].filter((m) => esAdmin || !m.admin);

  const pendientes = clientes.filter((c) => c.estado === "Pendiente");

  return (
    <main className="dashboard">
      {/* ---------- BARRA LATERAL ---------- */}
      <aside className="dashboard-sidebar">
        <div className="dashboard-logo">
          <div className="dashboard-logo-icon">
            <Wifi size={25} />
          </div>
          <span>ENLÁSATE</span>
        </div>

        <div className="sidebar-section">
          <span className="sidebar-label">MENÚ PRINCIPAL</span>

          {menu.map(({ id, label, Icono }) => (
            <button
              key={id}
              className={`sidebar-link ${seccion === id ? "active" : ""}`}
              onClick={() => setSeccion(id)}
            >
              <Icono size={19} />
              {label}
            </button>
          ))}
        </div>

        <div className="sidebar-bottom">
          <div className="sidebar-user">
            <div className="sidebar-avatar">
              <User size={20} />
            </div>
            <div>
              <strong>{usuario.nombre || "Usuario"}</strong>
              <span>{esAdmin ? "Administrador" : "Empleado"}</span>
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
        </div>
      </aside>

      {/* ---------- CONTENIDO ---------- */}
      <section className="dashboard-main">
        <header className="dashboard-header">
          <div>
            <span className="dashboard-date">
              {esAdmin ? "PANEL DE ADMINISTRADOR" : "PANEL DE EMPLEADO"}
            </span>
            <h1>Hola, {usuario.nombre || "bienvenido"}.</h1>
            <p>
              {seccion === "clientes" && "Da de alta clientes, gestiona sus códigos y registra sus pagos."}
              {seccion === "planes" && "Catálogo de planes de internet y equipos en renta."}
              {seccion === "pagos" && "Historial de ingresos y facturación."}
              {seccion === "soporte" && "Tickets de falla reportados por los clientes."}
              {seccion === "usuarios" && "Alta y baja de empleados del sistema."}
            </p>
          </div>

          <div className="dashboard-profile">
            <div className="profile-icon">
              <User size={21} />
            </div>
            <div>
              <strong>{usuario.nombre}</strong>
              <span>{usuario.email}</span>
            </div>
          </div>
        </header>

        {/* ================= CLIENTES ================= */}
        {seccion === "clientes" && (
          <>
            {/* --- código recién generado --- */}
            {creado && (
              <div className="panel-codigo">
                <div>
                  <span className="panel-codigo-tag">CÓDIGO DE ACTIVACIÓN (válido 24 h)</span>
                  <strong className="panel-codigo-valor">{creado.codigoActivacion}</strong>
                  <div className="panel-acciones-fila">
                    <button
                      type="button"
                      className="panel-btn-mini"
                      onClick={() => {
                        navigator.clipboard?.writeText(creado.codigoActivacion || "");
                      }}
                    >
                      Copiar código
                    </button>
                  </div>
                  <small>
                    Para {creado.nombre} · {creado.email}
                    <br />
                    Vence el {fechaCorta(creado.codigoExpira)}
                  </small>
                </div>
                <button className="panel-codigo-cerrar" onClick={() => setCreado(null)}>
                  <X size={17} />
                </button>
              </div>
            )}

            {/* --- barra de acciones --- */}
            <div className="panel-barra">
              <div className="panel-buscador">
                <Search size={17} />
                <input
                  type="search"
                  placeholder="Buscar por nombre, apellido o correo…"
                  value={texto}
                  onChange={(e) => setTexto(e.target.value)}
                />
              </div>

              <div className="panel-acciones">
                <span className="panel-total">{total} clientes</span>
                <button
                  className="panel-btn-primario"
                  onClick={() => {
                    setNuevo((v) => !v);
                    if (planes.length === 0) {
                      api.get("/planes").then(({ data }) => setPlanes(data)).catch(() => {});
                    }
                  }}
                >
                  <UserPlus size={17} />
                  Nuevo cliente
                </button>
              </div>
            </div>

            {/* --- formulario de alta --- */}
            {nuevo && (
              <form className="panel-form" onSubmit={crearCliente}>
                <div className="panel-form-grid">
                  <label>
                    Nombre
                    <input
                      value={form.nombre}
                      onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                      required
                      maxLength={60}
                    />
                  </label>
                  <label>
                    Apellido
                    <input
                      value={form.apellido}
                      onChange={(e) => setForm({ ...form, apellido: e.target.value })}
                      maxLength={60}
                    />
                  </label>
                  <label>
                    Correo
                    <input
                      type="email"
                      value={form.email}
                      onChange={(e) => setForm({ ...form, email: e.target.value })}
                      required
                      maxLength={100}
                    />
                  </label>
                  <label>
                    Teléfono (10 dígitos)
                    <input
                      value={form.telefono}
                      onChange={(e) => setForm({ ...form, telefono: e.target.value })}
                      required
                      maxLength={10}
                      pattern="\d{10}"
                    />
                  </label>

                  <label>
                    Plan contratado
                    <select
                      value={form.idPlan}
                      onChange={(e) => setForm({ ...form, idPlan: e.target.value })}
                    >
                      <option value="">Sin plan por ahora</option>
                      {planes.map((p) => (
                        <option key={p.idPlan} value={p.idPlan}>
                          {p.nombrePlan} · ${Number(p.precioMensual).toFixed(0)}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>

                <label className="panel-check">
                  <input
                    type="checkbox"
                    checked={form.conCuenta}
                    onChange={(e) => setForm({ ...form, conCuenta: e.target.checked })}
                  />
                  <span>
                    Crear cuenta en la aplicación
                    <small>
                      Si lo desmarcas, el cliente queda registrado sin cuenta y solo
                      tú podrás cobrarle y administrarlo.
                    </small>
                  </span>
                </label>

                <div className="panel-form-acciones">
                  <button type="button" className="panel-btn-sec" onClick={() => setNuevo(false)}>
                    Cancelar
                  </button>
                  <button type="submit" className="panel-btn-primario" disabled={creando}>
                    {creando ? "Guardando…" : "Dar de alta y generar código"}
                  </button>
                </div>
              </form>
            )}

            {error && <div className="panel-error">{error}</div>}

            {cargando ? (
              <div className="panel-estado">
                <LoaderCircle className="girando" size={22} /> Cargando clientes…
              </div>
            ) : clientes.length === 0 ? (
              <div className="panel-estado">No se encontraron clientes.</div>
            ) : (
              <div className="panel-tabla-scroll">
                <table className="panel-tabla">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Nombre</th>
                      <th>Correo</th>
                      <th>Plan</th>
                      <th>Estado</th>
                      <th>Código</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {clientes.map((c) => (
                      <tr key={c.idCliente}>
                        <td>{c.idCliente}</td>
                        <td>
                          <button
                            className="panel-link"
                            title="Ver ficha y meses del cliente"
                            onClick={() =>
                              navigate(
                                `/${esAdmin ? "admin" : "empleado"}/cliente/${c.idCliente}`
                              )
                            }
                          >
                            {c.nombre}
                          </button>
                        </td>
                        <td>{c.email}</td>
                        <td>{c.plan || <em>sin plan</em>}</td>
                        <td>
                          <span className={`chip ${String(c.estado).toLowerCase()}`}>
                            {c.estado}
                          </span>
                        </td>
                        <td>
                          {c.estado === "Pendiente" ? (
                            <span className="panel-code-chip">{c.codigoActivacion}</span>
                          ) : (
                            <span className="panel-mudo">—</span>
                          )}
                        </td>
                        <td>
                          <div className="panel-acciones-fila">
                            {c.estado === "Pendiente" && (
                              <>
                                <button
                                  className="panel-btn-mini"
                                  title="Activar servicio"
                                  onClick={() => activarCliente(c)}
                                >
                                  <CheckCircle2 size={16} /> Activar
                                </button>
                                <button
                                  className="panel-btn-mini gris"
                                  title="Generar código nuevo"
                                  onClick={() => regenerarCodigo(c)}
                                >
                                  <KeyRound size={16} />
                                </button>
                              </>
                            )}

                            {c.estado !== "Pendiente" && (
                              <button
                                className="panel-btn-mini"
                                title="Registrar pago en efectivo o depósito"
                                onClick={() => abrirCobro(c)}
                              >
                                <Wallet size={16} /> Cobrar
                              </button>
                            )}

                            {esAdmin && (
                              <button
                                className="panel-btn-mini rojo"
                                title="Eliminar cliente"
                                onClick={() => setPorBorrar(c)}
                              >
                                <Trash2 size={16} />
                              </button>
                            )}

                            <button
                              className="panel-btn-mini gris"
                              title="Editar datos del cliente"
                              onClick={() => abrirEdicionCliente(c)}
                            >
                              <Pencil size={16} />
                            </button>

                            {c.conCuenta && (
                              <button
                                className="panel-btn-mini gris"
                                title="Restablecer contraseña a Enlasate2026!"
                                onClick={() => setResetCliente(c)}
                              >
                                <KeyRound size={16} />
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            <div className="panel-paginacion">
              <button
                onClick={() => setPagina((p) => Math.max(0, p - 1))}
                disabled={pagina === 0 || cargando}
              >
                « Anterior
              </button>
              <span>
                Página {totalPaginas === 0 ? 0 : pagina + 1} de {totalPaginas}
                {pendientes.length > 0 && ` · ${pendientes.length} pendientes en esta página`}
              </span>
              <button
                onClick={() => setPagina((p) => Math.min(totalPaginas - 1, p + 1))}
                disabled={pagina >= totalPaginas - 1 || cargando}
              >
                Siguiente »
              </button>
            </div>
          </>
        )}

        {/* ================= PLANES ================= */}
        {seccion === "planes" && (
          <>
            {esAdmin && (
              <div className="panel-barra">
                <span className="panel-total">{planes.length} planes</span>
                <button
                  className="panel-btn-primario"
                  onClick={() => {
                    setNuevoPlan((v) => !v);
                    setFormPlan({ nombrePlan: "", velocidadMb: "", precioMensual: "" });
                  }}
                >
                  <Plus size={17} />
                  Nuevo plan
                </button>
              </div>
            )}

            {error && <div className="panel-error">{error}</div>}

            {nuevoPlan && esAdmin && (
              <form className="panel-form" onSubmit={crearPlan}>
                <div className="panel-form-grid">
                  <label>
                    Nombre del plan
                    <input
                      value={formPlan.nombrePlan}
                      onChange={(e) => setFormPlan({ ...formPlan, nombrePlan: e.target.value })}
                      required
                      maxLength={50}
                      placeholder="Ej. Residencial 30 MB"
                    />
                  </label>
                  <label>
                    Velocidad (Mbps)
                    <input
                      type="number"
                      min="1"
                      value={formPlan.velocidadMb}
                      onChange={(e) => setFormPlan({ ...formPlan, velocidadMb: e.target.value })}
                      required
                    />
                  </label>
                  <label>
                    Precio mensual
                    <input
                      type="number"
                      step="0.01"
                      min="1"
                      value={formPlan.precioMensual}
                      onChange={(e) => setFormPlan({ ...formPlan, precioMensual: e.target.value })}
                      required
                    />
                  </label>
                </div>
                <div className="panel-form-acciones">
                  <button type="button" className="panel-btn-sec" onClick={() => setNuevoPlan(false)}>
                    Cancelar
                  </button>
                  <button type="submit" className="panel-btn-primario" disabled={guardandoPlan}>
                    {guardandoPlan ? "Guardando…" : "Crear plan"}
                  </button>
                </div>
              </form>
            )}

            <div className="panel-grid">
              {planes.map((p) => (
                <article key={p.idPlan} className="panel-card">
                  <div className="panel-card-icono"><Network size={22} /></div>
                  <span className="panel-card-tag">PLAN {String(p.idPlan).padStart(2, "0")}</span>
                  <h3>{p.nombrePlan}</h3>
                  <div className="panel-card-precio">
                    {Number(p.velocidadMb)} <small>Mbps</small>
                  </div>
                  <p>${Number(p.precioMensual).toFixed(2)} al mes</p>

                  {esAdmin && (
                    <div className="panel-acciones-fila" style={{ marginTop: "12px" }}>
                      <button
                        className="panel-btn-mini gris"
                        onClick={() => {
                          setEditPlan(p);
                          setFormPlan({
                            nombrePlan: p.nombrePlan,
                            velocidadMb: String(p.velocidadMb),
                            precioMensual: String(p.precioMensual),
                          });
                        }}
                      >
                        <Pencil size={16} /> Editar
                      </button>
                      <button
                        className="panel-btn-mini rojo"
                        onClick={() => setPorBorrarPlan(p)}
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  )}
                </article>
              ))}
            </div>
          </>
        )}

        {/* ================= PAGOS ================= */}
        {seccion === "pagos" && (
          <>
            {esAdmin && (
              <div className="panel-barra">
                <div className="panel-acciones">
                  <select
                    className="panel-select"
                    value={anioRep}
                    onChange={(e) => setAnioRep(e.target.value)}
                  >
                    {[0, 1, 2, 3, 4].map((d) => {
                      const a = new Date().getFullYear() - d;
                      return <option key={a} value={a}>{a}</option>;
                    })}
                  </select>

                  <select
                    className="panel-select"
                    value={mesRep}
                    onChange={(e) => setMesRep(e.target.value)}
                  >
                    <option value="">Todo el año</option>
                    {["Enero","Febrero","Marzo","Abril","Mayo","Junio",
                      "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"]
                      .map((m, i) => (
                        <option key={m} value={String(i + 1).padStart(2, "0")}>{m}</option>
                      ))}
                  </select>

                  <button
                    className="panel-btn-primario"
                    onClick={descargarReporte}
                    disabled={bajandoPdf}
                  >
                    {bajandoPdf ? "Generando…" : "Descargar reporte PDF"}
                  </button>
                </div>
              </div>
            )}

            {pagos.length === 0 ? (
              <div className="panel-estado">Todavía no hay pagos registrados.</div>
            ) : (
              <div className="panel-tabla-scroll">
                <table className="panel-tabla">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Cliente</th>
                      <th>Mes</th>
                      <th>Monto</th>
                      <th>Método</th>
                      <th>Estado</th>
                      <th>Fecha</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pagos.map((p) => (
                      <tr key={p.idPago}>
                        <td>{p.idPago}</td>
                        <td>{p.cliente || p.idCliente}</td>
                        <td>{p.mesCorrespondiente}</td>
                        <td>${Number(p.monto).toFixed(2)}</td>
                        <td>{p.metodoPago}</td>
                        <td>
                          <span className="chip activo">{p.estadoPago || "Completado"}</span>
                        </td>
                        <td>{p.fechaPago}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            <div className="panel-paginacion">
              <button
                onClick={() => setPaginaPagos((p) => Math.max(0, p - 1))}
                disabled={paginaPagos === 0}
              >
                « Anterior
              </button>
              <span>
                Página {totalPaginasPagos === 0 ? 0 : paginaPagos + 1} de {totalPaginasPagos}
              </span>
              <button
                onClick={() => setPaginaPagos((p) => Math.min(totalPaginasPagos - 1, p + 1))}
                disabled={paginaPagos >= totalPaginasPagos - 1}
              >
                Siguiente »
              </button>
            </div>
          </>
        )}

        {/* ================= USUARIOS (solo admin) ================= */}
        {seccion === "usuarios" && esAdmin && (
          <>
            <div className="panel-barra">
              <span className="panel-total">{empleados.length} empleados</span>
              <button className="panel-btn-primario" onClick={() => setNuevoEmp((v) => !v)}>
                <UserPlus size={17} />
                Nuevo empleado
              </button>
            </div>

            {nuevoEmp && (
              <form className="panel-form" onSubmit={crearEmpleado}>
                <div className="panel-form-grid">
                  <label>
                    Nombre
                    <input
                      value={formEmp.nombre}
                      onChange={(e) => setFormEmp({ ...formEmp, nombre: e.target.value })}
                      required
                      maxLength={60}
                    />
                  </label>
                  <label>
                    Apellido
                    <input
                      value={formEmp.apellido}
                      onChange={(e) => setFormEmp({ ...formEmp, apellido: e.target.value })}
                      maxLength={60}
                    />
                  </label>
                  <label>
                    Correo
                    <input
                      type="email"
                      value={formEmp.email}
                      onChange={(e) => setFormEmp({ ...formEmp, email: e.target.value })}
                      required
                      maxLength={100}
                    />
                  </label>
                  <label>
                    Teléfono (10 dígitos)
                    <input
                      value={formEmp.telefono}
                      onChange={(e) => setFormEmp({ ...formEmp, telefono: e.target.value })}
                      required
                      maxLength={10}
                      pattern="\d{10}"
                    />
                  </label>
                  <label>
                    Puesto
                    <input
                      value={formEmp.puesto}
                      onChange={(e) => setFormEmp({ ...formEmp, puesto: e.target.value })}
                      required
                      maxLength={100}
                      placeholder="Ej. Ejecutiva de atención"
                    />
                  </label>
                </div>

                <p className="panel-nota">
                  Se crea con la contraseña temporal <strong>Enlasate2026!</strong> y
                  debe cambiarla al entrar.
                </p>

                <div className="panel-form-acciones">
                  <button type="button" className="panel-btn-sec" onClick={() => setNuevoEmp(false)}>
                    Cancelar
                  </button>
                  <button type="submit" className="panel-btn-primario" disabled={creandoEmp}>
                    {creandoEmp ? "Guardando…" : "Dar de alta empleado"}
                  </button>
                </div>
              </form>
            )}

            {error && <div className="panel-error">{error}</div>}

            {empleados.length === 0 ? (
              <div className="panel-estado">Todavía no hay empleados registrados.</div>
            ) : (
              <div className="panel-tabla-scroll">
                <table className="panel-tabla">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Nombre</th>
                      <th>Correo</th>
                      <th>Teléfono</th>
                      <th>Puesto</th>
                      <th>Rol</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {empleados.map((e) => (
                      <tr key={e.idEmpleado}>
                        <td>{e.idEmpleado}</td>
                        <td>{e.nombre}</td>
                        <td>{e.email}</td>
                        <td>{e.telefono}</td>
                        <td>{e.puesto}</td>
                        <td>
                          <span className="chip activo">{e.rol || "Empleado"}</span>
                        </td>
                        <td>
                          <div className="panel-acciones-fila">
                            <button
                              className="panel-btn-mini gris"
                              title="Editar empleado"
                              onClick={() => abrirEdicionEmp(e)}
                            >
                              <Pencil size={16} />
                            </button>

                            <button
                              className="panel-btn-mini rojo"
                              title="Dar de baja"
                              onClick={() => setPorBorrarEmp(e)}
                            >
                              <Trash2 size={16} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}

        {/* ================= SOPORTE ================= */}
        {seccion === "soporte" && (
          <>
            {error && <div className="panel-error">{error}</div>}

            {cargandoTickets ? (
              <div className="panel-estado">
                <LoaderCircle className="girando" size={22} /> Cargando tickets…
              </div>
            ) : tickets.length === 0 ? (
              <div className="panel-estado">No hay tickets de soporte.</div>
            ) : (
              <div className="panel-tabla-scroll">
                <table className="panel-tabla">
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Cliente</th>
                      <th>Falla reportada</th>
                      <th>Reportado</th>
                      <th>Atiende</th>
                      <th>Estado</th>
                    </tr>
                  </thead>
                  <tbody>
                    {tickets.map((t) => (
                      <tr key={t.idTicket}>
                        <td>{t.idTicket}</td>
                        <td>{t.nombreCliente || t.idCliente}</td>
                        <td>{t.descripcionFalla}</td>
                        <td>{String(t.fechaReporte || "").replace("T", " ").slice(0, 16)}</td>
                        <td>{t.nombreEmpleado || "—"}</td>
                        <td>
                          <select
                            className="panel-select"
                            value={t.estadoTicket}
                            onChange={(e) => cambiarEstadoTicket(t, e.target.value)}
                          >
                            <option>Abierto</option>
                            <option>En proceso</option>
                            <option>Cerrado</option>
                          </select>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            <div className="panel-paginacion">
              <button
                onClick={() => setPaginaTickets((p) => Math.max(0, p - 1))}
                disabled={paginaTickets === 0}
              >
                « Anterior
              </button>
              <span>
                Página {totalPaginasTickets === 0 ? 0 : paginaTickets + 1} de {totalPaginasTickets}
              </span>
              <button
                onClick={() => setPaginaTickets((p) => Math.min(totalPaginasTickets - 1, p + 1))}
                disabled={paginaTickets >= totalPaginasTickets - 1}
              >
                Siguiente »
              </button>
            </div>
          </>
        )}

        {/* ================= EN CONSTRUCCIÓN ================= */}
        {false && (
          <div className="panel-estado">Esta sección está en desarrollo.</div>
        )}
      </section>

      {/* ---------- modal: cobro ---------- */}
      {cobrar && (
        <div className="modal-fondo" onClick={() => setCobrar(null)}>
          <form className="modal-caja" onClick={(e) => e.stopPropagation()} onSubmit={registrarCobro}>
            <h3 className="modal-titulo">Registrar pago</h3>
            <p className="modal-mensaje">
              {cobrar.nombre} · {cobrar.email}
            </p>

            <label className="panel-form-label">
              Monto
              <input
                type="number"
                step="0.01"
                min="1"
                value={cobro.monto}
                onChange={(e) => setCobro({ ...cobro, monto: e.target.value })}
                required
              />
            </label>

            <label className="panel-form-label">
              Mes que cubre
              <input
                type="month"
                value={cobro.mes}
                onChange={(e) => setCobro({ ...cobro, mes: e.target.value })}
                required
              />
            </label>

            <label className="panel-form-label">
              Método
              <select
                value={cobro.metodo}
                onChange={(e) => setCobro({ ...cobro, metodo: e.target.value })}
              >
                <option>Efectivo</option>
                <option>Deposito</option>
                <option>Transferencia</option>
                <option>Tarjeta</option>
              </select>
            </label>

            <div className="modal-acciones">
              <button type="button" className="modal-btn secundario" onClick={() => setCobrar(null)}>
                Cancelar
              </button>
              <button type="submit" className="modal-btn peligro" disabled={cobrando}>
                {cobrando ? "Guardando…" : "Registrar pago"}
              </button>
            </div>
          </form>
        </div>
      )}

      <ModalConfirmacion
        abierto={!!porBorrar}
        titulo="Eliminar cliente"
        mensaje={
          porBorrar
            ? `¿Seguro que quieres eliminar a ${porBorrar.nombre}? Esta acción no se puede deshacer.`
            : ""
        }
        cargando={borrando}
        onConfirmar={confirmarBorrado}
        onCancelar={() => setPorBorrar(null)}
      />

      <ModalConfirmacion
        abierto={!!porBorrarEmp}
        titulo="Dar de baja empleado"
        mensaje={
          porBorrarEmp
            ? `¿Seguro que quieres dar de baja a ${porBorrarEmp.nombre}? Perderá el acceso al sistema y sus clientes quedarán sin asesor.`
            : ""
        }
        textoConfirmar="Sí, dar de baja"
        cargando={borrandoEmp}
        onConfirmar={borrarEmpleado}
        onCancelar={() => setPorBorrarEmp(null)}
      />

      <CambiarPassword
        abierto={cambiarPass}
        onCerrar={() => setCambiarPass(false)}
      />

      {/* ---------- modal: editar plan ---------- */}
      {editPlan && (
        <div className="modal-fondo" onClick={() => setEditPlan(null)}>
          <form className="modal-caja" onClick={(e) => e.stopPropagation()} onSubmit={guardarEditPlan}>
            <h3 className="modal-titulo">Editar plan</h3>
            <p className="modal-mensaje">PLAN {String(editPlan.idPlan).padStart(2, "0")}</p>

            <label className="ficha-campo">
              Nombre del plan
              <input
                value={formPlan.nombrePlan}
                onChange={(e) => setFormPlan({ ...formPlan, nombrePlan: e.target.value })}
                required
                maxLength={50}
              />
            </label>

            <label className="ficha-campo">
              Velocidad (Mbps)
              <input
                type="number"
                min="1"
                value={formPlan.velocidadMb}
                onChange={(e) => setFormPlan({ ...formPlan, velocidadMb: e.target.value })}
                required
              />
            </label>

            <label className="ficha-campo">
              Precio mensual
              <input
                type="number"
                step="0.01"
                min="1"
                value={formPlan.precioMensual}
                onChange={(e) => setFormPlan({ ...formPlan, precioMensual: e.target.value })}
                required
              />
            </label>

            <div className="modal-acciones">
              <button type="button" className="modal-btn secundario" onClick={() => setEditPlan(null)}>
                Cancelar
              </button>
              <button type="submit" className="modal-btn peligro" disabled={guardandoPlan}>
                {guardandoPlan ? "Guardando…" : "Guardar cambios"}
              </button>
            </div>
          </form>
        </div>
      )}

      <ModalConfirmacion
        abierto={!!porBorrarPlan}
        titulo="Eliminar plan"
        mensaje={
          porBorrarPlan
            ? `¿Seguro que quieres eliminar el plan "${porBorrarPlan.nombrePlan}"? Si tiene clientes asignados no se podrá borrar.`
            : ""
        }
        cargando={borrandoPlan}
        onConfirmar={borrarPlan}
        onCancelar={() => setPorBorrarPlan(null)}
      />

      <ModalConfirmacion
        abierto={!!resetCliente}
        titulo="Restablecer contraseña"
        mensaje={
          resetCliente
            ? `La contraseña de ${resetCliente.nombre} volverá a ser Enlasate2026! y deberá cambiarla al entrar.`
            : ""
        }
        textoConfirmar="Sí, restablecer"
        cargando={reseteando}
        onConfirmar={hacerResetPassword}
        onCancelar={() => setResetCliente(null)}
      />

      {/* ---------- modal: editar cliente ---------- */}
      {editCliente && (
        <div className="modal-fondo" onClick={() => setEditCliente(null)}>
          <form className="modal-caja" onClick={(e) => e.stopPropagation()} onSubmit={guardarCliente}>
            <h3 className="modal-titulo">Editar cliente</h3>
            <p className="modal-mensaje">{editCliente.email}</p>

            <label className="ficha-campo">
              Nombre
              <input
                value={formEditC.nombre}
                onChange={(e) => setFormEditC({ ...formEditC, nombre: e.target.value })}
                required
                maxLength={60}
              />
            </label>

            <label className="ficha-campo">
              Apellido
              <input
                value={formEditC.apellido}
                onChange={(e) => setFormEditC({ ...formEditC, apellido: e.target.value })}
                maxLength={60}
              />
            </label>

            <label className="ficha-campo">
              Correo
              <input
                type="email"
                value={formEditC.email}
                onChange={(e) => setFormEditC({ ...formEditC, email: e.target.value })}
                required
                maxLength={100}
              />
            </label>

            <label className="ficha-campo">
              Teléfono
              <input
                value={formEditC.telefono}
                onChange={(e) => setFormEditC({ ...formEditC, telefono: e.target.value })}
                required
                maxLength={10}
                pattern="\d{10}"
              />
            </label>

            <label className="ficha-campo">
              Plan
              <select
                value={formEditC.idPlan}
                onChange={(e) => setFormEditC({ ...formEditC, idPlan: e.target.value })}
                required
              >
                <option value="">Selecciona un plan</option>
                {planes.map((p) => (
                  <option key={p.idPlan} value={p.idPlan}>
                    {p.nombrePlan} · ${Number(p.precioMensual).toFixed(0)}
                  </option>
                ))}
              </select>
            </label>

            <label className="ficha-campo">
              Estado
              <select
                value={formEditC.estado}
                onChange={(e) => setFormEditC({ ...formEditC, estado: e.target.value })}
              >
                <option>Activo</option>
                <option>Inactivo</option>
                <option>Moroso</option>
                <option>Pendiente</option>
              </select>
            </label>

            <div className="modal-acciones">
              <button type="button" className="modal-btn secundario" onClick={() => setEditCliente(null)}>
                Cancelar
              </button>
              <button type="submit" className="modal-btn peligro" disabled={guardandoEditC}>
                {guardandoEditC ? "Guardando…" : "Guardar cambios"}
              </button>
            </div>

            {!editCliente.conCuenta && (
              <div className="panel-cuenta-aviso">
                <strong>Este cliente no tiene cuenta en la web</strong>
                <small>
                  Se le creará con el correo de arriba y la contraseña temporal
                  <b> Enlasate2026!</b>. Recibirá un código para activar su cuenta.
                </small>
                <button
                  type="button"
                  className="panel-btn-primario"
                  onClick={crearCuentaWeb}
                  disabled={creandoCuenta}
                >
                  {creandoCuenta ? "Creando cuenta…" : "Crear cuenta web y generar código"}
                </button>
              </div>
            )}
          </form>
        </div>
      )}

      {/* ---------- modal: editar empleado ---------- */}
      {editEmp && (
        <div className="modal-fondo" onClick={() => setEditEmp(null)}>
          <form className="modal-caja" onClick={(e) => e.stopPropagation()} onSubmit={guardarEmpleado}>
            <h3 className="modal-titulo">Editar empleado</h3>
            <p className="modal-mensaje">{editEmp.email}</p>

            <label className="ficha-campo">
              Nombre
              <input
                value={formEditE.nombre}
                onChange={(e) => setFormEditE({ ...formEditE, nombre: e.target.value })}
                required
                maxLength={60}
              />
            </label>

            <label className="ficha-campo">
              Apellido
              <input
                value={formEditE.apellido}
                onChange={(e) => setFormEditE({ ...formEditE, apellido: e.target.value })}
                maxLength={60}
              />
            </label>

            <label className="ficha-campo">
              Correo
              <input
                type="email"
                value={formEditE.email}
                onChange={(e) => setFormEditE({ ...formEditE, email: e.target.value })}
                required
                maxLength={100}
              />
            </label>

            <label className="ficha-campo">
              Teléfono
              <input
                value={formEditE.telefono}
                onChange={(e) => setFormEditE({ ...formEditE, telefono: e.target.value })}
                required
                maxLength={10}
                pattern="\d{10}"
              />
            </label>

            <label className="ficha-campo">
              Puesto
              <input
                value={formEditE.puesto}
                onChange={(e) => setFormEditE({ ...formEditE, puesto: e.target.value })}
                required
                maxLength={100}
              />
            </label>

            <div className="modal-acciones">
              <button type="button" className="modal-btn secundario" onClick={() => setEditEmp(null)}>
                Cancelar
              </button>
              <button type="submit" className="modal-btn peligro" disabled={guardandoEditE}>
                {guardandoEditE ? "Guardando…" : "Guardar cambios"}
              </button>
            </div>
          </form>
        </div>
      )}
    </main>
  );
}

export default AdminPanel;
