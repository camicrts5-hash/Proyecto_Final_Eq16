import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { ArrowRight, ArrowLeft, MessageCircle, LogOut } from "lucide-react";
import api from "../services/api";
import "./PendienteInstalacion.css";

// Convierte un teléfono a formato wa.me (solo dígitos, con 52 si es de México)
function aWhatsApp(tel) {
  if (!tel) return null;
  const solo = String(tel).replace(/\D/g, "");
  if (solo.length === 10) return "52" + solo;
  return solo;
}

function PendienteInstalacion() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const esCambio = params.get("cambio") === "1";
  const usuario = JSON.parse(localStorage.getItem("usuario") || "{}");

  const [empleados, setEmpleados] = useState([]);
  const [elegido, setElegido] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const [codigo, setCodigo] = useState("");
  const [error, setError] = useState("");
  const [activando, setActivando] = useState(false);
  const [planes, setPlanes] = useState([]);
  const [verPlanes, setVerPlanes] = useState(false);
  const [estadoCuenta, setEstadoCuenta] = useState(null);

  useEffect(() => {
    api.get("/empleados")
      .then(({ data }) => setEmpleados(data))
      .catch(() => setEmpleados([]));

    api.get("/clientes/mi-plan")
      .then(({ data }) => setEstadoCuenta(data))
      .catch(() => setEstadoCuenta(null));
  }, []);

  const abrirPlanes = () => {
    setVerPlanes((v) => !v);
    if (planes.length === 0) {
      api.get("/planes")
        .then(({ data }) => setPlanes(data))
        .catch(() => setPlanes([]));
    }
  };

  const contactar = async () => {
    if (!elegido) return;
    setError("");
    setEnviando(true);

    try {
      await api.put("/clientes/mi-empleado", { idEmpleado: elegido.idEmpleado });
    } catch (e) {
      setError("No se pudo guardar tu asesor. Intenta de nuevo.");
      setEnviando(false);
      return;
    }

    setEnviando(false);

    const numero = aWhatsApp(elegido.telefono);
    if (!numero) {
      setError("Tu asesor no tiene teléfono registrado. Escríbenos por otro medio.");
      return;
    }

    const texto = encodeURIComponent(
      `Hola ${elegido.nombre}, soy ${usuario.nombre || "un nuevo cliente"} de ENLÁSATE. ` +
      `Acabo de registrarme y quiero contratar internet. Mi correo es ${usuario.email || ""}.`
    );

    window.open(`https://wa.me/${numero}?text=${texto}`, "_blank");
  };

  const activar = async (event) => {
    event.preventDefault();
    setError("");
    setActivando(true);

    try {
      await api.post("/clientes/mi-activacion", { codigo: codigo.trim() });
      navigate("/dashboard", { replace: true });
    } catch (e) {
      setError(
        e?.response?.data?.message ||
        e?.response?.data?.mensaje ||
        "El código no es correcto."
      );
    } finally {
      setActivando(false);
    }
  };

  const cerrarSesion = async () => {
    try { await api.post("/auth/logout"); } catch (e) {}
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    navigate("/login", { replace: true });
  };

  return (
    <main className="pend">
      <div className="pend-caja">
        <button className="pend-volver" onClick={() => navigate("/dashboard")}>
          <ArrowLeft size={16} />
          Volver a mi panel
        </button>

        <p className="pend-marca">ENLÁSATE</p>

        <h1>
          {estadoCuenta && estadoCuenta.estado === "Activo"
            ? `Hola, ${usuario.nombre || "bienvenido"}.`
            : `Hola, ${usuario.nombre || "bienvenido"}.`}
        </h1>

        <p className="pend-texto">
          {esCambio
            ? "Para cambiar de plan ponte en contacto con tu asesor. Él revisa la disponibilidad en tu zona y hace el cambio en el sistema."
            : estadoCuenta && estadoCuenta.estado === "Activo"
              ? "Tu servicio ya está activo. Elige al asesor que te atenderá y, si ya te dio tu código de activación, ingrésalo abajo. Si todavía no lo tienes, escríbele por WhatsApp."
              : "Tu cuenta está creada, pero tu servicio todavía no empieza a correr. Elige a un asesor, contáctalo por WhatsApp y él agendará la instalación. Cuando te entregue el equipo te dará un código para activar tu cuenta."}
        </p>

        <hr className="pend-linea" />

        <h2>Elige a tu asesor</h2>

        {empleados.length === 0 && (
          <p className="pend-vacio">Cargando asesores…</p>
        )}

        <div className="pend-lista">
          {empleados.map((e) => (
            <button
              key={e.idEmpleado}
              type="button"
              className={"pend-fila" + (elegido?.idEmpleado === e.idEmpleado ? " activa" : "")}
              onClick={() => setElegido(e)}
            >
              <span className="pend-avatar">
                {(e.nombre || "?").charAt(0).toUpperCase()}
              </span>
              <span className="pend-datos">
                <strong>{e.nombre}</strong>
                <small>
                  {e.puesto}
                  <span className="pend-rol">{e.rol || "Empleado"}</span>
                </small>
              </span>
              <span className="pend-check">
                {elegido?.idEmpleado === e.idEmpleado ? "✓" : ""}
              </span>
            </button>
          ))}
        </div>

        <button
          className="pend-boton"
          onClick={contactar}
          disabled={!elegido || enviando}
        >
          <MessageCircle size={18} />
          {enviando ? "Guardando…" : "Contactar por WhatsApp"}
        </button>

        <button className="pend-boton plano" onClick={abrirPlanes}>
          {verPlanes ? "Ocultar los planes" : "Ver los planes disponibles"}
        </button>

        {verPlanes && (
          <div className="pend-planes">
            {planes.length === 0 && (
              <p className="pend-vacio">Cargando planes…</p>
            )}
            {planes.map((p) => (
              <div className="pend-plan" key={p.idPlan}>
                <div>
                  <strong>{p.nombrePlan}</strong>
                  <small>{Number(p.velocidadMb)} Mbps de velocidad</small>
                </div>
                <span className="pend-plan-precio">
                  ${Number(p.precioMensual).toFixed(0)}
                  <small>/mes</small>
                </span>
              </div>
            ))}
            <p className="pend-nota">
              Los precios son mensuales. El cobro empieza cuando actives tu cuenta
              con el código de tu asesor.
            </p>
          </div>
        )}

        {!esCambio && (
          <>
            <hr className="pend-linea" />

            <h2>¿Ya tienes tu código?</h2>
            <p className="pend-texto chico">
              Te lo da tu asesor cuando instala el equipo. Al activarlo empieza a
              contar tu primer mes.
            </p>

            <form className="pend-codigo" onSubmit={activar}>
              <input
                type="text"
                placeholder="ENL-0000"
                value={codigo}
                onChange={(e) => setCodigo(e.target.value.toUpperCase())}
                maxLength={10}
              />
              <button type="submit" disabled={codigo.trim().length < 4 || activando}>
                {activando ? "Activando…" : "Activar"}
                <ArrowRight size={17} />
              </button>
            </form>
          </>
        )}

        {error && <p className="pend-error">{error}</p>}

        <button className="pend-salir" onClick={cerrarSesion}>
          <LogOut size={15} />
          Cerrar sesión
        </button>
      </div>
    </main>
  );
}

export default PendienteInstalacion;
