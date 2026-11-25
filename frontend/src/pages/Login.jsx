import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Wifi,
  Mail,
  Lock,
  User,
  Phone,
  Eye,
  EyeOff,
  ArrowRight,
  ArrowLeft,
  Radio,
} from "lucide-react";
import api from "../services/api";

function Login() {
  const navigate = useNavigate();

  const [modoRegistro, setModoRegistro] = useState(
    window.location.pathname === "/registro"
  );

  const [mostrarPassword, setMostrarPassword] = useState(false);
  const [mostrarConfirmacion, setMostrarConfirmacion] = useState(false);

  const [login, setLogin] = useState({
    email: "",
    password: "",
  });

  const [registro, setRegistro] = useState({
    usuario: "",
    nombre: "",
    email: "",
    telefono: "",
    password: "",
    confirmarPassword: "",
  });

  const [errorLogin, setErrorLogin] = useState("");
  const [errorRegistro, setErrorRegistro] = useState("");
  const [cargando, setCargando] = useState(false);

  const cambiarModo = (registroActivo) => {
    setModoRegistro(registroActivo);
    setErrorLogin("");
    setErrorRegistro("");
    navigate(registroActivo ? "/registro" : "/login", { replace: true });
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setErrorLogin("");

    if (!login.email.trim() || !login.password) {
      setErrorLogin("Ingresa tu correo y contraseña.");
      return;
    }

    try {
      setCargando(true);

      const respuesta = await api.post("/auth/login", login);
      const datos = respuesta.data;

      localStorage.setItem("token", datos.token);
      localStorage.setItem(
        "usuario",
        JSON.stringify({
          idUsuario: datos.idUsuario,
          nombre: datos.nombre,
          email: datos.email,
          rol: datos.rol,
        })
      );
const rol = String(datos.rol || "").toUpperCase();

if (rol === "ADMIN") {
  navigate("/admin");
} else if (rol === "EMPLEADO") {
  navigate("/empleado");
} else {
  navigate("/dashboard");
}
      
    } catch (err) {
      setErrorLogin(
        err.response?.status === 401
          ? "Correo o contraseña incorrectos."
          : "No se pudo conectar con el servidor."
      );
    } finally {
      setCargando(false);
    }
  };

  const handleRegistro = async (e) => {
    e.preventDefault();
    setErrorRegistro("");

    const { usuario, nombre, email, telefono, password, confirmarPassword } =
      registro;

    if (
      !usuario.trim() ||
      !nombre.trim() ||
      !email.trim() ||
      !telefono.trim() ||
      !password ||
      !confirmarPassword
    ) {
      setErrorRegistro("Completa todos los campos.");
      return;
    }

    if (!/^[0-9]{10}$/.test(telefono)) {
      setErrorRegistro("El teléfono debe tener 10 dígitos.");
      return;
    }

    if (
      !/^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$/.test(password)
    ) {
      setErrorRegistro(
        "Usa 8 caracteres, una mayúscula, un número y un carácter especial."
      );
      return;
    }

    if (password !== confirmarPassword) {
      setErrorRegistro("Las contraseñas no coinciden.");
      return;
    }

    try {
      setCargando(true);

      const respuesta = await api.post("/auth/registro", {
        usuario,
        nombre,
        email,
        telefono,
        password,
      });

      const datos = respuesta.data;

      localStorage.setItem("token", datos.token);
      localStorage.setItem(
        "usuario",
        JSON.stringify({
          idUsuario: datos.idUsuario,
          nombre: datos.nombre,
          email: datos.email,
          rol: datos.rol,
        })
      );

      navigate("/pendiente");
    } catch (err) {
      setErrorRegistro(
        err.response?.data?.mensaje ||
          "No se pudo completar el registro. Verifica tus datos."
      );
    } finally {
      setCargando(false);
    }
  };

  return (
    <main className="auth-page">
      <header className="auth-brand">
        <div className="auth-brand-icon">
          <Wifi size={27} strokeWidth={2.5} />
        </div>
        <span>ENLÁSATE</span>
      </header>

      <section className={`auth-container ${modoRegistro ? "active" : ""}`}>
        {/* INICIAR SESIÓN */}
        <div className="auth-form-panel login-panel">
          <div className="auth-form-content">
            <span className="auth-eyebrow">BIENVENIDO DE NUEVO</span>

            <h1>Iniciar sesión</h1>

            <p className="auth-description">
              Ingresa tus datos para continuar conectado.
            </p>

            <form onSubmit={handleLogin} noValidate>
              <label className="auth-label">Correo electrónico</label>

              <div className="auth-input">
                <Mail size={19} />
                <input
                  type="email"
                  placeholder="ejemplo@correo.com"
                  value={login.email}
                  onChange={(e) =>
                    setLogin({ ...login, email: e.target.value })
                  }
                />
              </div>

              <label className="auth-label">Contraseña</label>

              <div className="auth-input">
                <Lock size={19} />
                <input
                  type={mostrarPassword ? "text" : "password"}
                  placeholder="Ingresa tu contraseña"
                  value={login.password}
                  onChange={(e) =>
                    setLogin({ ...login, password: e.target.value })
                  }
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setMostrarPassword(!mostrarPassword)}
                >
                  {mostrarPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>

              <div className="auth-forgot">
                <button type="button" onClick={() => navigate("/recuperar")}>
                  ¿Olvidaste tu contraseña?
                </button>
              </div>

              {errorLogin && (
                <p className="auth-error">{errorLogin}</p>
              )}

              <button className="auth-submit" disabled={cargando}>
                {cargando ? "INICIANDO..." : "INICIAR SESIÓN"}
                <ArrowRight size={19} />
              </button>
            </form>

            <p className="auth-mobile-switch">
              ¿No tienes cuenta?{" "}
              <button onClick={() => cambiarModo(true)}>Regístrate</button>
            </p>
          </div>
        </div>

        {/* REGISTRO */}
        <div className="auth-form-panel register-panel">
          <div className="auth-form-content">
            <span className="auth-eyebrow">COMIENZA AQUÍ</span>

            <h1>Crear una cuenta</h1>

            <p className="auth-description">
              Regístrate y descubre todo lo que ENLÁSATE tiene para ti.
            </p>

            <form onSubmit={handleRegistro} noValidate>
              <label className="auth-label">Nombre de usuario</label>

              <div className="auth-input">
                <User size={19} />
                <input
                  type="text"
                  placeholder="Tu nombre de usuario"
                  maxLength={40}
                  value={registro.usuario}
                  onChange={(e) =>
                    setRegistro({ ...registro, usuario: e.target.value })
                  }
                />
              </div>

              <label className="auth-label">Nombre</label>

              <div className="auth-input">
                <User size={19} />
                <input
                  type="text"
                  placeholder="Tu nombre"
                  maxLength={60}
                  value={registro.nombre}
                  onChange={(e) =>
                    setRegistro({ ...registro, nombre: e.target.value })
                  }
                />
              </div>

              <label className="auth-label">Correo electrónico</label>

              <div className="auth-input">
                <Mail size={19} />
                <input
                  type="email"
                  placeholder="ejemplo@correo.com"
                  value={registro.email}
                  onChange={(e) =>
                    setRegistro({ ...registro, email: e.target.value })
                  }
                />
              </div>

              <label className="auth-label">Teléfono</label>

              <div className="auth-input">
                <Phone size={19} />
                <input
                  type="tel"
                  placeholder="10 dígitos"
                  maxLength={10}
                  value={registro.telefono}
                  onChange={(e) =>
                    setRegistro({
                      ...registro,
                      telefono: e.target.value.replace(/\D/g, ""),
                    })
                  }
                />
              </div>

              <label className="auth-label">Contraseña</label>

              <div className="auth-input">
                <Lock size={19} />
                <input
                  type={mostrarPassword ? "text" : "password"}
                  placeholder="Crea una contraseña"
                  value={registro.password}
                  onChange={(e) =>
                    setRegistro({ ...registro, password: e.target.value })
                  }
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setMostrarPassword(!mostrarPassword)}
                >
                  {mostrarPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>

              <label className="auth-label">Confirmar contraseña</label>

              <div className="auth-input">
                <Lock size={19} />
                <input
                  type={mostrarConfirmacion ? "text" : "password"}
                  placeholder="Repite tu contraseña"
                  value={registro.confirmarPassword}
                  onChange={(e) =>
                    setRegistro({
                      ...registro,
                      confirmarPassword: e.target.value,
                    })
                  }
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() =>
                    setMostrarConfirmacion(!mostrarConfirmacion)
                  }
                >
                  {mostrarConfirmacion ? (
                    <EyeOff size={18} />
                  ) : (
                    <Eye size={18} />
                  )}
                </button>
              </div>

              {errorRegistro && (
                <p className="auth-error">{errorRegistro}</p>
              )}

              <button className="auth-submit" disabled={cargando}>
                {cargando ? "CREANDO CUENTA..." : "CREAR CUENTA"}
                <ArrowRight size={19} />
              </button>
            </form>

            <p className="auth-mobile-switch">
              ¿Ya tienes cuenta?{" "}
              <button onClick={() => cambiarModo(false)}>
                Inicia sesión
              </button>
            </p>
          </div>
        </div>

        {/* PANEL NARANJA ANIMADO */}
        <div className="auth-overlay">
          <div className="overlay-content overlay-login">
            <div className="overlay-icon">
              <Radio size={36} />
            </div>

            <span className="overlay-tag">SIEMPRE CONECTADOS</span>

            <h2>¡Hola de nuevo!</h2>

            <p>
              Nos alegra tenerte aquí. Inicia sesión y sigue disfrutando de
              una conexión que te acerca.
            </p>

            <button
              className="overlay-button"
              onClick={() => cambiarModo(true)}
            >
              CREAR CUENTA
              <ArrowRight size={18} />
            </button>
          </div>

          <div className="overlay-content overlay-register">
            <div className="overlay-icon">
              <Wifi size={36} />
            </div>

            <span className="overlay-tag">FORMA PARTE DE ENLÁSATE</span>

            <h2>¡Únete a nosotros!</h2>

            <p>
              Crea tu cuenta hoy. Después podrás conocer nuestros planes y
              elegir el que mejor se adapte a ti.
            </p>

            <button
              className="overlay-button"
              onClick={() => cambiarModo(false)}
            >
              <ArrowLeft size={18} />
              INICIAR SESIÓN
            </button>
          </div>
        </div>
      </section>

      <footer className="auth-footer">
        © 2026 ENLÁSATE · Conectando personas
      </footer>
    </main>
  );
}

export default Login;