import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import {
  Wifi, Gauge, Users, Radio, Receipt, LifeBuoy, UserCog,
  LogOut, User, LoaderCircle, Search, Trash2, Network,
  UserPlus, KeyRound, CheckCircle2, Wallet, X, Pencil, Plus, MapPin, RefreshCw,
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
  const [form, setForm] = useState({
    nombre: "", apellido: "", email: "", telefono: "",
    conCuenta: true, idPlan: "",
    idEstado: "", idMunicipio: "", idColonia: "",
    calle: "", numero: "", colonia: "", codigoPostal: "",
  });
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

  // ---------- mi perfil (solo admin) ----------
  const [editPerfil, setEditPerfil] = useState(false);
  const [formPerfil, setFormPerfil] = useState({ nombre: "", apellido: "", email: "" });
  const [guardandoPerfil, setGuardandoPerfil] = useState(false);

  const abrirPerfil = () => {
    setFormPerfil({
      nombre: usuario.nombre || "",
      apellido: "",
      email: usuario.email || "",
    });
    setEditPerfil(true);
  };

  const guardarPerfil = async (event) => {
    event.preventDefault();
    setGuardandoPerfil(true);
    setError("");
    try {
      const { data } = await api.put("/auth/perfil", formPerfil);

      // Se guarda el token y los datos nuevos (el correo cambió)
      localStorage.setItem("token", data.token);
      localStorage.setItem("usuario", JSON.stringify({
        idUsuario: data.idUsuario,
        nombre: data.nombre,
        email: data.email,
        rol: data.rol,
      }));

      setEditPerfil(false);
      window.location.reload();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo guardar el perfil.");
    } finally {
      setGuardandoPerfil(false);
    }
  };

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
  const [copiado, setCopiado] = useState(false);

  // ---------- clientes por zona ----------
  const [verZonas, setVerZonas] = useState(false);
  const [zonas, setZonas] = useState([]);

  // ---------- municipios y filtros ----------
  const [municipios, setMunicipios] = useState([]);
  const [estados, setEstados] = useState([]);
  const [nuevoMunicipio, setNuevoMunicipio] = useState("");
  const [agregandoMunicipio, setAgregandoMunicipio] = useState(false);
  const [editandoMunicipio, setEditandoMunicipio] = useState(null);

  // ---------- catálogo de colonias y estados ----------
  const [colonias, setColonias] = useState([]);
  const [editorZona, setEditorZona] = useState(null); // { tipo, id, valor }
  const [zonaEstado, setZonaEstado] = useState("");
  const [zonaMunicipio, setZonaMunicipio] = useState("");
  const [zonaColonia, setZonaColonia] = useState("");

  const cargarColonias = (idMunicipio) => {
    if (!idMunicipio) {
      setColonias([]);
      return;
    }

    api.get("/colonias", { params: { idMunicipio: Number(idMunicipio) } })
      .then(({ data }) => setColonias(data))
      .catch(() => setColonias([]));
  };

  const abrirEditorZona = (tipo, id, valor) => {
    setEditorZona({ tipo, id, valor });
  };

  const guardarZona = async () => {
    if (!editorZona) return;

    const valor = (editorZona.valor || "").trim();

    if (valor.length < 3) {
      setError("El nombre debe tener al menos 3 letras.");
      return;
    }

    try {
      if (editorZona.tipo === "estado") {
        const { data } = editorZona.id
          ? await api.put(`/estados/${editorZona.id}`, { nombreEstado: valor })
          : await api.post("/estados", { nombreEstado: valor });

        setEstados((lista) => {
          const otras = lista.filter((e) => e.idEstado !== data.idEstado);
          return [...otras, data].sort((a, b) => a.nombreEstado.localeCompare(b.nombreEstado));
        });

        setZonaEstado(String(data.idEstado));
        setForm((f) => ({ ...f, idEstado: String(data.idEstado) }));
      }

      if (editorZona.tipo === "municipio") {
        const { data } = editorZona.id
          ? await api.put(`/municipios/${editorZona.id}`, {
              nombreMunicipio: valor,
              idEstado: Number(zonaEstado || 1),
            })
          : await api.post("/municipios", {
              nombreMunicipio: valor,
              idEstado: Number(zonaEstado || 1),
            });

        setMunicipios((lista) => {
          const otros = lista.filter((m) => m.idMunicipio !== data.idMunicipio);
          return [...otros, data].sort((a, b) => a.nombreMunicipio.localeCompare(b.nombreMunicipio));
        });

        setZonaMunicipio(String(data.idMunicipio));
        cargarColonias(data.idMunicipio);
      }

      if (editorZona.tipo === "colonia") {
        const idMunicipio = Number(zonaMunicipio || form.idMunicipio);

        const { data } = editorZona.id
          ? await api.put(`/colonias/${editorZona.id}`, {
              nombreColonia: valor,
              idMunicipio,
            })
          : await api.post("/colonias", {
              nombreColonia: valor,
              idMunicipio,
            });

        setColonias((lista) => {
          const otras = lista.filter((c) => c.idColonia !== data.idColonia);
          return [...otras, data].sort((a, b) => a.nombreColonia.localeCompare(b.nombreColonia));
        });

        setZonaColonia(String(data.idColonia));
        setForm((f) => ({ ...f, idColonia: String(data.idColonia), colonia: data.nombreColonia }));
      }

      setEditorZona(null);
      setError("");
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo guardar.");
    }
  };
  const [filtros, setFiltros] = useState({
    estado: "", conCuenta: "", pagoMes: "", idEstado: "", idMunicipio: "", colonia: "",
  });

  const cargarMunicipios = () => {
    if (municipios.length === 0) {
      api.get("/municipios").then(({ data }) => setMunicipios(data)).catch(() => {});
    }
  };

  const cargarEstados = () => {
    if (estados.length === 0) {
      api.get("/estados").then(({ data }) => setEstados(data)).catch(() => setEstados([]));
    }
  };

  const agregarMunicipio = async () => {
    const nombre = nuevoMunicipio.trim();

    if (nombre.length < 3) {
      setError("Escribe el nombre del municipio (mínimo 3 letras).");
      return;
    }

    try {
      const { data } = await api.post("/municipios", {
        nombreMunicipio: nombre,
        idEstado: 1,
      });

      setMunicipios((lista) => [...lista, data]);
      setForm((f) => ({ ...f, idMunicipio: String(data.idMunicipio) }));
      setNuevoMunicipio("");
      setAgregandoMunicipio(false);
      setError("");
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo agregar el municipio.");
    }
  };

  // ---------- editar domicilio del cliente ----------
  const [domCliente, setDomCliente] = useState(null);
  const [formDom, setFormDom] = useState({
    idDomicilio: null, idMunicipio: "", idColonia: "",
    calle: "", numero: "", colonia: "", codigoPostal: "",
  });
  const [guardandoDom, setGuardandoDom] = useState(false);

  const abrirDomicilio = async (cliente) => {
    cargarMunicipios();
    setDomCliente(cliente);
    setError("");

    try {
      const { data } = await api.get(`/domicilios/cliente/${cliente.idCliente}`);
      const d = Array.isArray(data) && data.length > 0 ? data[0] : null;

      if (d) {
        setFormDom({
          idDomicilio: d.idDomicilio,
          idMunicipio: String(d.idMunicipio || ""),
          idColonia: d.idColonia ? String(d.idColonia) : "",
          calle: d.calle || "",
          numero: d.numero || "",
          colonia: d.colonia || "",
          codigoPostal: d.codigoPostal || "",
        });

        if (d.idMunicipio) {
          api.get("/colonias", { params: { idMunicipio: d.idMunicipio } })
            .then(({ data: cols }) => setColonias(Array.isArray(cols) ? cols : []))
            .catch(() => setColonias([]));
        }
      } else {
        setFormDom({
          idDomicilio: null, idMunicipio: "", idColonia: "",
          calle: "", numero: "", colonia: "", codigoPostal: "",
        });
        setColonias([]);
      }
    } catch (e) {
      setFormDom({
        idDomicilio: null, idMunicipio: "", idColonia: "",
        calle: "", numero: "", colonia: "", codigoPostal: "",
      });
    }
  };

  const guardarDomicilio = async (event) => {
    event.preventDefault();
    setError("");

    if (!formDom.idMunicipio || !formDom.colonia.trim() || !formDom.calle.trim()
        || !formDom.numero.trim() || !/^\d{5}$/.test(formDom.codigoPostal.trim())) {
      setError("Completa municipio, colonia, calle, número y código postal de 5 dígitos.");
      return;
    }

    setGuardandoDom(true);

    const cuerpo = {
      idCliente: domCliente.idCliente,
      idMunicipio: Number(formDom.idMunicipio),
      idColonia: formDom.idColonia ? Number(formDom.idColonia) : null,
      calle: formDom.calle.trim(),
      numero: formDom.numero.trim(),
      colonia: formDom.colonia.trim(),
      codigoPostal: formDom.codigoPostal.trim(),
    };

    try {
      if (formDom.idDomicilio) {
        await api.put(`/domicilios/${formDom.idDomicilio}`, cuerpo);
      } else {
        await api.post("/domicilios", cuerpo);
      }

      setDomCliente(null);
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo guardar el domicilio.");
    } finally {
      setGuardandoDom(false);
    }
  };
  const abrirEditarMunicipio = () => {
    const actual = municipios.find((m) => String(m.idMunicipio) === String(form.idMunicipio));

    if (!actual) {
      setError("Primero selecciona un municipio para editarlo.");
      return;
    }

    setEditandoMunicipio(actual.idMunicipio);
    setNuevoMunicipio(actual.nombreMunicipio);
    setAgregandoMunicipio(false);
  };

  const guardarMunicipioEditado = async () => {
    const nombre = nuevoMunicipio.trim();

    if (nombre.length < 3) {
      setError("El nombre del municipio debe tener al menos 3 letras.");
      return;
    }

    try {
      const { data } = await api.put(`/municipios/${editandoMunicipio}`, {
        nombreMunicipio: nombre,
        idEstado: 1,
      });

      setMunicipios((lista) =>
        lista.map((m) => (m.idMunicipio === data.idMunicipio ? data : m))
      );
      setEditandoMunicipio(null);
      setNuevoMunicipio("");
      setError("");
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo editar el municipio.");
    }
  };

  const abrirZonas = () => {
    setVerZonas((v) => !v);
    if (zonas.length === 0) {
      api.get("/clientes/por-zona")
        .then(({ data }) => setZonas(data))
        .catch(() => setZonas([]));
    }
  };

  const copiarCodigo = (texto) => {
    navigator.clipboard?.writeText(texto || "");
    setCopiado(true);
    setTimeout(() => setCopiado(false), 2000);
  };

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

  // ---------- alta de ticket ----------
  const [nuevoTicket, setNuevoTicket] = useState(false);
  const [formTicket, setFormTicket] = useState({ idCliente: "", idEmpleado: "", descripcion: "" });
  const [guardandoTicket, setGuardandoTicket] = useState(false);
  const [clientesLista, setClientesLista] = useState([]);

  const abrirNuevoTicket = () => {
    setNuevoTicket((v) => !v);
    if (clientesLista.length === 0) {
      api.get("/clientes", { params: { page: 0, size: 100 } })
        .then(({ data }) => setClientesLista(data.content || []))
        .catch(() => {});
    }
    if (empleados.length === 0) cargarEmpleados();
  };

  const crearTicket = async (event) => {
    event.preventDefault();
    setGuardandoTicket(true);
    setError("");
    try {
      await api.post("/soporte", {
        idCliente: Number(formTicket.idCliente),
        idEmpleado: Number(formTicket.idEmpleado),
        descripcionFalla: formTicket.descripcion,
        fechaReporte: new Date().toISOString().slice(0, 19),
        estadoTicket: "Abierto",
        fechaCierre: null,
      });
      setFormTicket({ idCliente: "", idEmpleado: "", descripcion: "" });
      setNuevoTicket(false);
      cargarTickets();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo crear el ticket.");
    } finally {
      setGuardandoTicket(false);
    }
  };

  const borrarTicket = async (t) => {
    if (!window.confirm(`¿Eliminar el ticket #${t.idTicket}?`)) return;
    setError("");
    try {
      await api.delete(`/soporte/${t.idTicket}`);
      cargarTickets();
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo eliminar el ticket.");
    }
  };

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
        params: {
          page: pagina,
          size: TAM,
          q: busqueda || undefined,
          estado: filtros.estado || undefined,
          conCuenta: filtros.conCuenta === "" ? undefined : filtros.conCuenta === "si",
          pagoMes: filtros.pagoMes === "" ? undefined : filtros.pagoMes === "si",
          idEstado: filtros.idEstado ? Number(filtros.idEstado) : undefined,
          idMunicipio: filtros.idMunicipio ? Number(filtros.idMunicipio) : undefined,
          colonia: filtros.colonia || undefined,
        },
      });
      setClientes(data.content || []);
      setTotalPaginas(data.totalPages || 0);
      setTotal(data.totalElements || 0);
    } catch (e) {
      const status = e?.response?.status;
      setError(
        status
          ? `No se pudo cargar la lista de clientes (error ${status}).`
          : "No se pudo cargar la lista de clientes. Revisa tu conexión."
      );
      setClientes([]);
    } finally {
      setCargando(false);
    }
  }, [pagina, busqueda, filtros]);

  useEffect(() => {
    if (seccion === "clientes") {
      cargarClientes();
      cargarEstados();
      cargarMunicipios();
    }

    if (seccion === "zonas") {
      cargarEstados();
      cargarMunicipios();
    }
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

    // Validación propia (mensajes en rojo, sin globos del navegador)
    if (!form.nombre.trim()) {
      setError("Escribe el nombre del cliente.");
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
      setError("El correo no tiene un formato válido. Ejemplo: cliente@correo.com");
      return;
    }
    if (!/^\d{10}$/.test(form.telefono.trim())) {
      setError("El teléfono debe tener exactamente 10 dígitos.");
      return;
    }
    if (!form.idMunicipio) {
      setError("Selecciona el municipio del cliente (o agrégalo con el botón +).");
      return;
    }
    if (!form.colonia.trim()) {
      setError("Selecciona la colonia (o agrégala con el botón +).");
      return;
    }
    if (!form.calle.trim()) {
      setError("Escribe la calle.");
      return;
    }
    if (!form.numero.trim()) {
      setError("Escribe el número exterior.");
      return;
    }
    if (!/^\d{5}$/.test(form.codigoPostal.trim())) {
      setError("El código postal debe tener exactamente 5 dígitos.");
      return;
    }

    setCreando(true);
    try {
      const { data } = await api.post("/clientes", {
        ...form,
        idPlan: form.idPlan ? Number(form.idPlan) : null,
        idMunicipio: Number(form.idMunicipio),
        idColonia: form.idColonia ? Number(form.idColonia) : null,
      });
      setCreado(data);
      setForm({
        nombre: "", apellido: "", email: "", telefono: "",
        conCuenta: true, idPlan: "",
        idEstado: "", idMunicipio: "", idColonia: "",
        calle: "", numero: "", colonia: "", codigoPostal: "",
      });
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
    { id: "zonas",    label: "Zonas y Colonias",    Icono: Network,   admin: true  },
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

          {esAdmin && (
            <button
              className="sidebar-logout"
              onClick={abrirPerfil}
              style={{ marginTop: "6px" }}
            >
              <UserCog size={18} />
              Mi perfil
            </button>
          )}
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
        {seccion === "zonas" && "Administra los estados, municipios y colonias que se usan en los domicilios."}
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
            {/* --- código recién generado (solo clientes CON cuenta) --- */}
            {creado && (
              <div className="panel-codigo">
                {creado.codigoActivacion ? (
                  <div>
                    <span className="panel-codigo-tag">CÓDIGO DE ACTIVACIÓN (válido 24 h)</span>
                    <strong className="panel-codigo-valor">{creado.codigoActivacion}</strong>
                    <div className="panel-acciones-fila">
                      <button
                        type="button"
                        className="panel-btn-mini"
                        onClick={() => copiarCodigo(creado.codigoActivacion)}
                      >
                        {copiado ? "¡Copiado!" : "Copiar código"}
                      </button>
                    </div>
                    <small>
                      Para {creado.nombre} · {creado.email}
                      <br />
                      Vence el {fechaCorta(creado.codigoExpira)}
                    </small>
                  </div>
                ) : (
                  <div>
                    <span className="panel-codigo-tag">CLIENTE SIN CUENTA EN LA WEB</span>
                    <strong className="panel-codigo-valor chico">
                      {creado.nombre}
                    </strong>
                    <small>
                      Quedó registrado y activo. No necesita código: tú lo
                      administras y le cobras desde su ficha.
                      <br />
                      {creado.email} · {creado.telefono}
                    </small>
                  </div>
                )}
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

                <button className="panel-btn-sec" onClick={abrirZonas}>
                  <Users size={17} />
                  {verZonas ? "Ocultar zonas" : "Ver por zona"}
                </button>

                <button
                  className="panel-btn-primario"
                  onClick={() => {
                    setNuevo((v) => !v);
                    if (planes.length === 0) {
                      api.get("/planes").then(({ data }) => setPlanes(data)).catch(() => {});
                    }
                    cargarMunicipios();
                  }}
                >
                  <UserPlus size={17} />
                  Nuevo cliente
                </button>
              </div>
            </div>

            {/* --- formulario de alta --- */}
            {nuevo && (
              <form className="panel-form" onSubmit={crearCliente} noValidate>
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

                  <label>
                    Estado
                    <div className="dom-fila">
                      <select
                        value={form.idEstado}
                        onChange={(e) =>
                          setForm({
                            ...form,
                            idEstado: e.target.value,
                            idMunicipio: "",
                            idColonia: "",
                            colonia: "",
                          })
                        }
                      >
                        <option value="">Selecciona estado</option>
                        {estados.map((e) => (
                          <option key={e.idEstado} value={e.idEstado}>
                            {e.nombreEstado}
                          </option>
                        ))}
                      </select>

                      <button
                        type="button"
                        className="panel-btn-mini"
                        title="Agregar estado"
                        onClick={() => abrirEditorZona("estado", null, "")}
                      >
                        <Plus size={16} />
                      </button>

                      <button
                        type="button"
                        className="panel-btn-mini"
                        title="Editar el estado seleccionado"
                        onClick={() => {
                          const est = estados.find((x) => String(x.idEstado) === String(form.idEstado));
                          if (!est) {
                            setError("Selecciona un estado para editarlo.");
                            return;
                          }
                          abrirEditorZona("estado", est.idEstado, est.nombreEstado);
                        }}
                      >
                        <Pencil size={15} />
                      </button>
                    </div>

                    {editorZona?.tipo === "estado" && (
                      <div className="dom-nuevo">
                        <input
                          placeholder="Nombre del estado"
                          value={editorZona.valor}
                          maxLength={60}
                          onChange={(ev) => setEditorZona({ ...editorZona, valor: ev.target.value })}
                        />
                        <button type="button" className="panel-btn-primario chico" onClick={guardarZona}>
                          {editorZona.id ? "Guardar" : "Agregar"}
                        </button>
                        <button type="button" className="panel-btn-sec chico" onClick={() => setEditorZona(null)}>
                          Cancelar
                        </button>
                      </div>
                    )}
                  </label>

                  <label>
                    Municipio
                    <div className="dom-fila">
                      <select
                        value={form.idMunicipio}
                        onChange={(e) => {
                          const valor = e.target.value;
                          setForm({ ...form, idMunicipio: valor, idColonia: "", colonia: "" });
                          cargarColonias(valor);
                        }}
                      >
                        <option value="">Selecciona municipio</option>
                        {municipios
                          .filter((m) => !form.idEstado || String(m.idEstado) === String(form.idEstado))
                          .map((m) => (
                            <option key={m.idMunicipio} value={m.idMunicipio}>
                              {m.nombreMunicipio}
                            </option>
                          ))}
                      </select>
                      <button
                        type="button"
                        className="panel-btn-mini"
                        title="Agregar municipio"
                        onClick={() => {
                          setAgregandoMunicipio((v) => !v);
                          setEditandoMunicipio(null);
                          setNuevoMunicipio("");
                        }}
                      >
                        <Plus size={16} />
                      </button>

                      <button
                        type="button"
                        className="panel-btn-mini"
                        title="Editar el municipio seleccionado"
                        onClick={abrirEditarMunicipio}
                      >
                        <Pencil size={15} />
                      </button>
                    </div>

                    {(agregandoMunicipio || editandoMunicipio) && (
                      <div className="dom-nuevo">
                        <input
                          placeholder={
                            editandoMunicipio ? "Nuevo nombre del municipio" : "Nombre del municipio"
                          }
                          value={nuevoMunicipio}
                          maxLength={80}
                          onChange={(e) => setNuevoMunicipio(e.target.value)}
                          onKeyDown={(e) => {
                            if (e.key === "Enter") {
                              e.preventDefault();
                              if (editandoMunicipio) guardarMunicipioEditado();
                              else agregarMunicipio();
                            }
                          }}
                        />

                        <button
                          type="button"
                          className="panel-btn-primario chico"
                          onClick={editandoMunicipio ? guardarMunicipioEditado : agregarMunicipio}
                        >
                          {editandoMunicipio ? "Guardar" : "Agregar"}
                        </button>

                        <button
                          type="button"
                          className="panel-btn-sec chico"
                          onClick={() => {
                            setAgregandoMunicipio(false);
                            setEditandoMunicipio(null);
                            setNuevoMunicipio("");
                          }}
                        >
                          Cancelar
                        </button>
                      </div>
                    )}
                  </label>

                  <label>
                    Colonia
                    <div className="dom-fila">
                      <select
                        value={form.idColonia}
                        onChange={(e) => {
                          const col = colonias.find((c) => String(c.idColonia) === String(e.target.value));
                          setForm({
                            ...form,
                            idColonia: e.target.value,
                            colonia: col ? col.nombreColonia : "",
                          });
                        }}
                      >
                        <option value="">Selecciona colonia</option>
                        {colonias.map((c) => (
                          <option key={c.idColonia} value={c.idColonia}>
                            {c.nombreColonia}
                          </option>
                        ))}
                      </select>

                      <button
                        type="button"
                        className="panel-btn-mini"
                        title="Agregar colonia"
                        onClick={() => {
                          if (!form.idMunicipio) {
                            setError("Primero selecciona el municipio.");
                            return;
                          }
                          abrirEditorZona("colonia", null, "");
                        }}
                      >
                        <Plus size={16} />
                      </button>

                      <button
                        type="button"
                        className="panel-btn-mini"
                        title="Editar la colonia seleccionada"
                        onClick={() => {
                          const col = colonias.find((c) => String(c.idColonia) === String(form.idColonia));
                          if (!col) {
                            setError("Selecciona una colonia para editarla.");
                            return;
                          }
                          abrirEditorZona("colonia", col.idColonia, col.nombreColonia);
                        }}
                      >
                        <Pencil size={15} />
                      </button>
                    </div>

                    {editorZona?.tipo === "colonia" && (
                      <div className="dom-nuevo">
                        <input
                          placeholder="Nombre de la colonia"
                          value={editorZona.valor}
                          maxLength={80}
                          onChange={(ev) => setEditorZona({ ...editorZona, valor: ev.target.value })}
                        />
                        <button type="button" className="panel-btn-primario chico" onClick={guardarZona}>
                          {editorZona.id ? "Guardar" : "Agregar"}
                        </button>
                        <button type="button" className="panel-btn-sec chico" onClick={() => setEditorZona(null)}>
                          Cancelar
                        </button>
                      </div>
                    )}
                  </label>

                  <label>
                    Calle
                    <input
                      value={form.calle}
                      onChange={(e) => setForm({ ...form, calle: e.target.value })}
                      required
                      maxLength={120}
                    />
                  </label>

                  <label>
                    Número
                    <input
                      value={form.numero}
                      onChange={(e) => setForm({ ...form, numero: e.target.value })}
                      required
                      maxLength={10}
                    />
                  </label>

                  <label>
                    Código postal
                    <input
                      value={form.codigoPostal}
                      onChange={(e) => setForm({ ...form, codigoPostal: e.target.value })}
                      required
                      maxLength={5}
                      pattern="\d{5}"
                    />
                  </label>
                </div>

                <div className="panel-tipo-cuenta">
                  <span className="panel-tipo-titulo">Tipo de cliente</span>

                  <label className={form.conCuenta ? "tipo-opcion activa" : "tipo-opcion"}>
                    <input
                      type="radio"
                      name="tipoCuenta"
                      checked={form.conCuenta === true}
                      onChange={() => setForm({ ...form, conCuenta: true })}
                    />
                    <span>
                      <strong>Con cuenta en la web</strong>
                      <small>Entra a la app con su correo y puede pagar en línea.</small>
                    </span>
                  </label>

                  <label className={!form.conCuenta ? "tipo-opcion activa" : "tipo-opcion"}>
                    <input
                      type="radio"
                      name="tipoCuenta"
                      checked={form.conCuenta === false}
                      onChange={() => setForm({ ...form, conCuenta: false })}
                    />
                    <span>
                      <strong>Sin cuenta</strong>
                      <small>No entra a la app; solo tú lo administras y le cobras.</small>
                    </span>
                  </label>
                </div>

                {error && <div className="panel-error">{error}</div>}

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

            <div className="panel-filtros">
              <select
                className="panel-select"
                value={filtros.idEstado}
                onChange={(e) => {
                  setFiltros({ ...filtros, idEstado: e.target.value, idMunicipio: "" });
                  setPagina(0);
                  cargarEstados();
                  cargarMunicipios();
                }}
              >
                <option value="">Todos los estados</option>
                {estados.map((e) => (
                  <option key={e.idEstado} value={e.idEstado}>
                    {e.nombreEstado}
                  </option>
                ))}
              </select>

              <select
                className="panel-select"
                value={filtros.idMunicipio}
                onChange={(e) => {
                  setFiltros({ ...filtros, idMunicipio: e.target.value });
                  setPagina(0);
                }}
              >
                <option value="">Todos los municipios</option>
                {municipios
                  .filter((m) => !filtros.idEstado || String(m.idEstado) === String(filtros.idEstado))
                  .map((m) => (
                    <option key={m.idMunicipio} value={m.idMunicipio}>
                      {m.nombreMunicipio}
                    </option>
                  ))}
              </select>

              <select
                className="panel-select"
                value={filtros.estado}
                onChange={(e) => {
                  setFiltros({ ...filtros, estado: e.target.value });
                  setPagina(0);
                }}
              >
                <option value="">Todos los estados de cuenta</option>
                <option value="Activo">Activo</option>
                <option value="Moroso">Moroso</option>
                <option value="Inactivo">Inactivo</option>
                <option value="Pendiente">Pendiente</option>
              </select>

              <select
                className="panel-select"
                value={filtros.pagoMes}
                onChange={(e) => {
                  setFiltros({ ...filtros, pagoMes: e.target.value });
                  setPagina(0);
                }}
              >
                <option value="">Pagos: todos</option>
                <option value="si">Ya pagaron este mes</option>
                <option value="no">Deben este mes</option>
              </select>

              <select
                className="panel-select"
                value={filtros.conCuenta}
                onChange={(e) => {
                  setFiltros({ ...filtros, conCuenta: e.target.value });
                  setPagina(0);
                }}
              >
                <option value="">Cuenta: todas</option>
                <option value="si">Con cuenta en la web</option>
                <option value="no">Sin cuenta en la web</option>
              </select>

              <input
                className="panel-select"
                placeholder="Colonia…"
                value={filtros.colonia}
                onChange={(e) => {
                  setFiltros({ ...filtros, colonia: e.target.value });
                  setPagina(0);
                }}
              />
            </div>

            {verZonas && (
              <div className="panel-tabla-scroll" style={{ marginBottom: "18px" }}>
                <table className="panel-tabla">
                  <thead>
                    <tr>
                      <th>Municipio</th>
                      <th>Colonia</th>
                      <th>Clientes</th>
                    </tr>
                  </thead>
                  <tbody>
                    {zonas.length === 0 ? (
                      <tr>
                        <td colSpan={3}>
                          Los clientes todavía no tienen domicilio registrado.
                        </td>
                      </tr>
                    ) : (
                      zonas.map((z, i) => (
                        <tr key={`${z.municipio}-${z.colonia}-${i}`}>
                          <td>{z.municipio}</td>
                          <td>{z.colonia}</td>
                          <td><strong>{z.total}</strong></td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            )}

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
                                  title="Generar código de activación nuevo"
                                  onClick={() => regenerarCodigo(c)}
                                >
                                  <RefreshCw size={16} />
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
                              title="Editar domicilio del cliente"
                              onClick={() => abrirDomicilio(c)}
                            >
                              <MapPin size={16} />
                            </button>

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

        {/* ================= ZONAS Y COLONIAS ================= */}
        {seccion === "zonas" && (
          <>
            {error && <div className="panel-error">{error}</div>}

            <div className="zona-admin">
              {/* ---------- estado ---------- */}
              <div className="zona-fila">
                <span>Estado</span>
                <div className="zona-controles">
                  <select
                    value={zonaEstado}
                    onChange={(e) => {
                      setZonaEstado(e.target.value);
                      setZonaMunicipio("");
                      setColonias([]);
                    }}
                  >
                    <option value="">Selecciona estado</option>
                    {estados.map((e) => (
                      <option key={e.idEstado} value={e.idEstado}>{e.nombreEstado}</option>
                    ))}
                  </select>

                  <button
                    type="button"
                    className="panel-btn-mini"
                    title="Agregar estado"
                    onClick={() => setEditorZona({ tipo: "estado", id: null, valor: "" })}
                  >
                    <Plus size={16} />
                  </button>

                  <button
                    type="button"
                    className="panel-btn-mini"
                    title="Editar estado"
                    onClick={() => {
                      const est = estados.find((x) => String(x.idEstado) === String(zonaEstado));
                      if (!est) { setError("Selecciona un estado para editarlo."); return; }
                      setEditorZona({ tipo: "estado", id: est.idEstado, valor: est.nombreEstado });
                    }}
                  >
                    <Pencil size={15} />
                  </button>
                </div>

                {editorZona?.tipo === "estado" && (
                  <div className="zona-editor">
                    <input
                      placeholder="Nombre del estado"
                      value={editorZona.valor}
                      maxLength={60}
                      onChange={(ev) => setEditorZona({ ...editorZona, valor: ev.target.value })}
                    />
                    <button type="button" className="panel-btn-primario chico" onClick={guardarZona}>
                      {editorZona.id ? "Guardar" : "Agregar"}
                    </button>
                    <button type="button" className="panel-btn-sec chico" onClick={() => setEditorZona(null)}>
                      Cancelar
                    </button>
                  </div>
                )}
              </div>

              {/* ---------- municipio ---------- */}
              <div className="zona-fila">
                <span>Municipio</span>
                <div className="zona-controles">
                  <select
                    value={zonaMunicipio}
                    onChange={(e) => {
                      setZonaMunicipio(e.target.value);
                      cargarColonias(e.target.value);
                    }}
                  >
                    <option value="">Selecciona municipio</option>
                    {municipios
                      .filter((m) => !zonaEstado || String(m.idEstado) === String(zonaEstado))
                      .map((m) => (
                        <option key={m.idMunicipio} value={m.idMunicipio}>{m.nombreMunicipio}</option>
                      ))}
                  </select>

                  <button
                    type="button"
                    className="panel-btn-mini"
                    title="Agregar municipio"
                    onClick={() => {
                      if (!zonaEstado) { setError("Primero selecciona el estado."); return; }
                      setEditorZona({ tipo: "municipio", id: null, valor: "" });
                    }}
                  >
                    <Plus size={16} />
                  </button>

                  <button
                    type="button"
                    className="panel-btn-mini"
                    title="Editar municipio"
                    onClick={() => {
                      const mun = municipios.find((x) => String(x.idMunicipio) === String(zonaMunicipio));
                      if (!mun) { setError("Selecciona un municipio para editarlo."); return; }
                      setEditorZona({ tipo: "municipio", id: mun.idMunicipio, valor: mun.nombreMunicipio });
                    }}
                  >
                    <Pencil size={15} />
                  </button>
                </div>

                {editorZona?.tipo === "municipio" && (
                  <div className="zona-editor">
                    <input
                      placeholder="Nombre del municipio"
                      value={editorZona.valor}
                      maxLength={80}
                      onChange={(ev) => setEditorZona({ ...editorZona, valor: ev.target.value })}
                    />
                    <button type="button" className="panel-btn-primario chico" onClick={guardarZona}>
                      {editorZona.id ? "Guardar" : "Agregar"}
                    </button>
                    <button type="button" className="panel-btn-sec chico" onClick={() => setEditorZona(null)}>
                      Cancelar
                    </button>
                  </div>
                )}
              </div>

              {/* ---------- colonia ---------- */}
              <div className="zona-fila">
                <span>Colonia</span>
                <div className="zona-controles">
                  <select
                    value={zonaColonia}
                    onChange={(e) => setZonaColonia(e.target.value)}
                  >
                    <option value="">Selecciona colonia</option>
                    {colonias.map((c) => (
                      <option key={c.idColonia} value={c.idColonia}>{c.nombreColonia}</option>
                    ))}
                  </select>

                  <button
                    type="button"
                    className="panel-btn-mini"
                    title="Agregar colonia"
                    onClick={() => {
                      if (!zonaMunicipio) { setError("Primero selecciona el municipio."); return; }
                      setEditorZona({ tipo: "colonia", id: null, valor: "" });
                    }}
                  >
                    <Plus size={16} />
                  </button>

                  <button
                    type="button"
                    className="panel-btn-mini"
                    title="Editar colonia"
                    onClick={() => {
                      const col = colonias.find((x) => String(x.idColonia) === String(zonaColonia));
                      if (!col) { setError("Selecciona una colonia para editarla."); return; }
                      setEditorZona({ tipo: "colonia", id: col.idColonia, valor: col.nombreColonia });
                    }}
                  >
                    <Pencil size={15} />
                  </button>
                </div>

                {editorZona?.tipo === "colonia" && (
                  <div className="zona-editor">
                    <input
                      placeholder="Nombre de la colonia"
                      value={editorZona.valor}
                      maxLength={80}
                      onChange={(ev) => setEditorZona({ ...editorZona, valor: ev.target.value })}
                    />
                    <button type="button" className="panel-btn-primario chico" onClick={guardarZona}>
                      {editorZona.id ? "Guardar" : "Agregar"}
                    </button>
                    <button type="button" className="panel-btn-sec chico" onClick={() => setEditorZona(null)}>
                      Cancelar
                    </button>
                  </div>
                )}

                <div className="zona-resumen">
                  {zonaMunicipio
                    ? `${colonias.length} colonia(s) registradas en este municipio`
                    : "Selecciona un municipio para ver sus colonias"}
                </div>
              </div>
            </div>
          </>
        )}

        {/* ================= SOPORTE ================= */}
        {seccion === "soporte" && (
          <>
            <div className="panel-barra">
              <span className="panel-total">{tickets.length} tickets en esta página</span>
              <button className="panel-btn-primario" onClick={abrirNuevoTicket}>
                <Plus size={17} />
                Nuevo ticket
              </button>
            </div>

            {nuevoTicket && (
              <form className="panel-form" onSubmit={crearTicket}>
                <div className="panel-form-grid">
                  <label>
                    Cliente
                    <select
                      value={formTicket.idCliente}
                      onChange={(e) => setFormTicket({ ...formTicket, idCliente: e.target.value })}
                      required
                    >
                      <option value="">Selecciona un cliente</option>
                      {clientesLista.map((c) => (
                        <option key={c.idCliente} value={c.idCliente}>
                          {c.nombre} · {c.email}
                        </option>
                      ))}
                    </select>
                  </label>

                  <label>
                    Atiende
                    <select
                      value={formTicket.idEmpleado}
                      onChange={(e) => setFormTicket({ ...formTicket, idEmpleado: e.target.value })}
                      required
                    >
                      <option value="">Selecciona un empleado</option>
                      {empleados.map((e) => (
                        <option key={e.idEmpleado} value={e.idEmpleado}>
                          {e.nombre}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>

                <label>
                  Falla reportada
                  <textarea
                    rows={3}
                    maxLength={255}
                    value={formTicket.descripcion}
                    onChange={(e) => setFormTicket({ ...formTicket, descripcion: e.target.value })}
                    required
                    placeholder="Describe la falla que reporta el cliente"
                  />
                </label>

                <div className="panel-form-acciones">
                  <button type="button" className="panel-btn-sec" onClick={() => setNuevoTicket(false)}>
                    Cancelar
                  </button>
                  <button type="submit" className="panel-btn-primario" disabled={guardandoTicket}>
                    {guardandoTicket ? "Guardando…" : "Crear ticket"}
                  </button>
                </div>
              </form>
            )}

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
                      <th></th>
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
                        <td>
                          <button
                            className="panel-btn-mini rojo"
                            title="Eliminar ticket"
                            onClick={() => borrarTicket(t)}
                          >
                            <Trash2 size={16} />
                          </button>
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

      {/* ---------- modal: mi perfil (admin) ---------- */}
      {editPerfil && (
        <div className="modal-fondo" onClick={() => setEditPerfil(false)}>
          <form className="modal-caja" onClick={(e) => e.stopPropagation()} onSubmit={guardarPerfil}>
            <h3 className="modal-titulo">Mi perfil</h3>
            <p className="modal-mensaje">Actualiza tu nombre y correo de acceso.</p>

            <label className="ficha-campo">
              Nombre
              <input
                value={formPerfil.nombre}
                onChange={(e) => setFormPerfil({ ...formPerfil, nombre: e.target.value })}
                required
                maxLength={60}
              />
            </label>

            <label className="ficha-campo">
              Apellido
              <input
                value={formPerfil.apellido}
                onChange={(e) => setFormPerfil({ ...formPerfil, apellido: e.target.value })}
                maxLength={60}
              />
            </label>

            <label className="ficha-campo">
              Correo
              <input
                type="email"
                value={formPerfil.email}
                onChange={(e) => setFormPerfil({ ...formPerfil, email: e.target.value })}
                required
                maxLength={120}
              />
            </label>

            {error && <div className="panel-error">{error}</div>}

            <div className="modal-acciones">
              <button type="button" className="modal-btn secundario" onClick={() => setEditPerfil(false)}>
                Cancelar
              </button>
              <button type="submit" className="modal-btn peligro" disabled={guardandoPerfil}>
                {guardandoPerfil ? "Guardando…" : "Guardar cambios"}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* ---------- modal: domicilio del cliente ---------- */}
      {domCliente && (
        <div className="modal-fondo" onClick={() => setDomCliente(null)}>
          <form className="modal-caja" onClick={(e) => e.stopPropagation()} onSubmit={guardarDomicilio} noValidate>
            <h3 className="modal-titulo">
              Domicilio de {domCliente.nombre}
            </h3>
            <p className="modal-mensaje">
              {formDom.idDomicilio
                ? "Corrige la dirección de instalación."
                : "Este cliente no tiene domicilio. Captúralo para poder instalarlo."}
            </p>

            <label className="ficha-campo">
              Municipio
              <select
                value={formDom.idMunicipio}
                onChange={(e) => {
                  const valor = e.target.value;
                  setFormDom({ ...formDom, idMunicipio: valor, idColonia: "", colonia: "" });
                  if (valor) {
                    api.get("/colonias", { params: { idMunicipio: Number(valor) } })
                      .then(({ data }) => setColonias(Array.isArray(data) ? data : []))
                      .catch(() => setColonias([]));
                  } else {
                    setColonias([]);
                  }
                }}
              >
                <option value="">Selecciona municipio</option>
                {municipios.map((m) => (
                  <option key={m.idMunicipio} value={m.idMunicipio}>{m.nombreMunicipio}</option>
                ))}
              </select>
            </label>

            <label className="ficha-campo">
              Colonia
              <select
                value={formDom.idColonia}
                onChange={(e) => {
                  const col = colonias.find((c) => String(c.idColonia) === String(e.target.value));
                  setFormDom({
                    ...formDom,
                    idColonia: e.target.value,
                    colonia: col ? col.nombreColonia : "",
                  });
                }}
              >
                <option value="">Selecciona colonia</option>
                {colonias.map((c) => (
                  <option key={c.idColonia} value={c.idColonia}>{c.nombreColonia}</option>
                ))}
              </select>
            </label>

            <label className="ficha-campo">
              Calle
              <input
                value={formDom.calle}
                onChange={(e) => setFormDom({ ...formDom, calle: e.target.value })}
                maxLength={120}
              />
            </label>

            <label className="ficha-campo">
              Número
              <input
                value={formDom.numero}
                onChange={(e) => setFormDom({ ...formDom, numero: e.target.value })}
                maxLength={10}
              />
            </label>

            <label className="ficha-campo">
              Código postal
              <input
                value={formDom.codigoPostal}
                onChange={(e) => setFormDom({ ...formDom, codigoPostal: e.target.value })}
                maxLength={5}
              />
            </label>

            {error && <div className="panel-error">{error}</div>}

            <div className="modal-acciones">
              <button type="button" className="modal-btn secundario" onClick={() => setDomCliente(null)}>
                Cancelar
              </button>
              <button type="submit" className="modal-btn peligro" disabled={guardandoDom}>
                {guardandoDom ? "Guardando…" : "Guardar domicilio"}
              </button>
            </div>
          </form>
        </div>
      )}

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
