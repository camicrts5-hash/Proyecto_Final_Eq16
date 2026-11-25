import { useState } from "react";
import api from "../services/api";
import "./componentes.css";
import "./CambiarPassword.css";

export default function CambiarPassword({ abierto, onCerrar }) {
  const [actual, setActual] = useState("");
  const [nueva, setNueva] = useState("");
  const [repetir, setRepetir] = useState("");
  const [error, setError] = useState("");
  const [ok, setOk] = useState("");
  const [cargando, setCargando] = useState(false);

  if (!abierto) return null;

  const limpiar = () => {
    setActual("");
    setNueva("");
    setRepetir("");
    setError("");
    setOk("");
  };

  const cerrar = () => {
    limpiar();
    onCerrar();
  };

  const enviar = async (event) => {
    event.preventDefault();
    setError("");
    setOk("");

    if (nueva.length < 8) {
      setError("La contraseña nueva debe tener al menos 8 caracteres.");
      return;
    }
    if (!/[A-Za-z]/.test(nueva) || !/\d/.test(nueva)) {
      setError("La contraseña nueva debe combinar letras y números.");
      return;
    }
    if (nueva !== repetir) {
      setError("Las contraseñas nuevas no coinciden.");
      return;
    }

    setCargando(true);
    try {
      await api.put("/auth/password", {
        passwordActual: actual,
        passwordNueva: nueva,
      });
      setOk("Contraseña actualizada. Úsala la próxima vez que entres.");
      setActual("");
      setNueva("");
      setRepetir("");
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo cambiar la contraseña.");
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="modal-fondo" onClick={cerrar}>
      <form className="modal-caja" onClick={(e) => e.stopPropagation()} onSubmit={enviar} noValidate>
        <h3 className="modal-titulo">Cambiar contraseña</h3>
        <p className="modal-mensaje">
          Escribe tu contraseña actual y la nueva. Mínimo 8 caracteres, con letras y números.
        </p>

        <label className="cp-label">
          Contraseña actual
          <input
            type="password"
            value={actual}
            onChange={(e) => setActual(e.target.value)}
            required
            autoComplete="current-password"
          />
        </label>

        <label className="cp-label">
          Contraseña nueva
          <input
            type="password"
            value={nueva}
            onChange={(e) => setNueva(e.target.value)}
            required
            autoComplete="new-password"
          />
        </label>

        <label className="cp-label">
          Repite la contraseña nueva
          <input
            type="password"
            value={repetir}
            onChange={(e) => setRepetir(e.target.value)}
            required
            autoComplete="new-password"
          />
        </label>

        {error && <p className="cp-error">{error}</p>}
        {ok && <p className="cp-ok">{ok}</p>}

        <div className="modal-acciones">
          <button type="button" className="modal-btn secundario" onClick={cerrar}>
            Cerrar
          </button>
          <button type="submit" className="modal-btn peligro" disabled={cargando}>
            {cargando ? "Guardando…" : "Cambiar contraseña"}
          </button>
        </div>
      </form>
    </div>
  );
}
