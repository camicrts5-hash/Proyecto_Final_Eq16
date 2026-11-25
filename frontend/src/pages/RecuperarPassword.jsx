import { useState } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { ArrowRight, Mail } from "lucide-react";
import api from "../services/api";
import "./PendienteInstalacion.css";
import "./RecuperarPassword.css";

function RecuperarPassword() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const token = params.get("token");

  const [email, setEmail] = useState("");
  const [nueva, setNueva] = useState("");
  const [repetir, setRepetir] = useState("");
  const [error, setError] = useState("");
  const [ok, setOk] = useState("");
  const [cargando, setCargando] = useState(false);

  // ---------------- pedir el enlace ----------------
  const pedirEnlace = async (event) => {
    event.preventDefault();
    setError("");

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      setError("Escribe un correo válido, por ejemplo nombre@dominio.com");
      return;
    }

    setCargando(true);
    try {
      const { data } = await api.post("/auth/recuperar", { email: email.trim() });
      setOk(
        data?.mensaje ||
          "Si el correo existe, te enviamos un enlace para restablecer tu contraseña."
      );
    } catch (e) {
      const d = e?.response?.data;
      setError(d?.message || d?.mensaje || "No se pudo enviar el correo.");
    } finally {
      setCargando(false);
    }
  };

  // ---------------- restablecer con el token ----------------
  const restablecer = async (event) => {
    event.preventDefault();
    setError("");

    if (nueva.length < 8) {
      setError("La contraseña debe tener al menos 8 caracteres.");
      return;
    }
    if (!/[A-Za-z]/.test(nueva) || !/\d/.test(nueva)) {
      setError("La contraseña debe combinar letras y números.");
      return;
    }
    if (nueva !== repetir) {
      setError("Las contraseñas no coinciden.");
      return;
    }

    setCargando(true);
    try {
      await api.post("/auth/restablecer", { token, passwordNueva: nueva });
      setOk("Contraseña actualizada. Te llevamos al inicio de sesión…");
      setTimeout(() => navigate("/login", { replace: true }), 1600);
    } catch (e) {
      const d = e?.response?.data;
      setError(
        d?.message || d?.mensaje || "El enlace venció o no es válido."
      );
    } finally {
      setCargando(false);
    }
  };

  return (
    <main className="pend">
      <div className="pend-caja rec-caja">
        <p className="pend-marca">ENLÁSATE</p>

        {token ? (
          <>
            <h1>Elige una contraseña nueva</h1>
            <p className="pend-texto">
              Mínimo 8 caracteres, combinando letras y números.
            </p>

            <form onSubmit={restablecer} noValidate className="rec-form">
              <label className="cp-label">
                Contraseña nueva
                <input
                  type="password"
                  value={nueva}
                  onChange={(e) => setNueva(e.target.value)}
                  autoComplete="new-password"
                />
              </label>

              <label className="cp-label">
                Repite la contraseña
                <input
                  type="password"
                  value={repetir}
                  onChange={(e) => setRepetir(e.target.value)}
                  autoComplete="new-password"
                />
              </label>

              {error && <p className="pend-error">{error}</p>}
              {ok && <p className="rec-ok">{ok}</p>}

              <button className="pend-boton" type="submit" disabled={cargando}>
                {cargando ? "Guardando…" : "Guardar contraseña"}
                <ArrowRight size={17} />
              </button>
            </form>
          </>
        ) : (
          <>
            <h1>¿Olvidaste tu contraseña?</h1>
            <p className="pend-texto">
              Escribe el correo con el que entras a ENLÁSATE y te mandamos un
              enlace para crear una nueva. El enlace vence en 30 minutos.
            </p>

            <form onSubmit={pedirEnlace} noValidate className="rec-form">
              <label className="cp-label">
                Correo electrónico
                <input
                  type="email"
                  placeholder="tucorreo@ejemplo.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  autoComplete="email"
                />
              </label>

              {error && <p className="pend-error">{error}</p>}
              {ok && <p className="rec-ok">{ok}</p>}

              <button className="pend-boton" type="submit" disabled={cargando}>
                <Mail size={17} />
                {cargando ? "Enviando…" : "Enviar enlace"}
              </button>
            </form>
          </>
        )}

        <Link to="/login" className="rec-volver">
          Volver al inicio de sesión
        </Link>
      </div>
    </main>
  );
}

export default RecuperarPassword;
