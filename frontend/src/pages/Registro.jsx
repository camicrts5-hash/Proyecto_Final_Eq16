import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../services/api";

function Registro() {
  const navigate = useNavigate();

  const [formulario, setFormulario] = useState({
    usuario: "",
    nombre: "",
    email: "",
    telefono: "",
    password: "",
    confirmarPassword: "",
  });

  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormulario({
      ...formulario,
      [name]: value,
    });
  };

  const handleRegistro = async (e) => {
    e.preventDefault();
    setError("");

    const { usuario, nombre, email, telefono, password, confirmarPassword } =
      formulario;

    if (!usuario.trim() || !nombre.trim() || !email.trim() ||
        !telefono.trim() || !password || !confirmarPassword) {
      setError("Todos los campos son obligatorios.");
      return;
    }

    if (!/^[0-9]{10}$/.test(telefono)) {
      setError("El teléfono debe tener 10 dígitos.");
      return;
    }

    if (!/^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$/.test(password)) {
      setError(
        "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial."
      );
      return;
    }

    if (password !== confirmarPassword) {
      setError("Las contraseñas no coinciden.");
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

      navigate("/dashboard");
    } catch (err) {
      if (err.response?.status === 400) {
        setError(
          err.response.data?.mensaje ||
          "No se pudo completar el registro. Verifica tus datos."
        );
      } else {
        setError("No se pudo conectar con el servidor.");
      }
    } finally {
      setCargando(false);
    }
  };

  return (
    <main className="login-container">
      <header className="brand-header">
        <span className="brand-name">Enlasate</span>
      </header>

      <section className="login-card">
        <div className="login-form-side registro-form-side">
          <h1>Crear cuenta</h1>

          <p className="login-subtitle">
            Regístrate para comenzar a utilizar ENLÁSATE
          </p>

          <form onSubmit={handleRegistro}>
            <div className="input-wrapper">
              <input
                type="text"
                name="usuario"
                placeholder="Nombre de usuario"
                value={formulario.usuario}
                onChange={handleChange}
                maxLength={40}
              />
            </div>

            <div className="input-wrapper">
              <input
                type="text"
                name="nombre"
                placeholder="Nombre"
                value={formulario.nombre}
                onChange={handleChange}
                maxLength={60}
              />
            </div>

            <div className="input-wrapper">
              <input
                type="email"
                name="email"
                placeholder="Correo electrónico"
                value={formulario.email}
                onChange={handleChange}
              />
            </div>

            <div className="input-wrapper">
              <input
                type="tel"
                name="telefono"
                placeholder="Teléfono (10 dígitos)"
                value={formulario.telefono}
                onChange={handleChange}
                maxLength={10}
              />
            </div>

            <div className="input-wrapper">
              <input
                type="password"
                name="password"
                placeholder="Contraseña"
                value={formulario.password}
                onChange={handleChange}
              />
            </div>

            <div className="input-wrapper">
              <input
                type="password"
                name="confirmarPassword"
                placeholder="Confirmar contraseña"
                value={formulario.confirmarPassword}
                onChange={handleChange}
              />
            </div>

            {error && <p className="login-error">{error}</p>}

            <button
              type="submit"
              className="btn-submit"
              disabled={cargando}
            >
              {cargando ? "CREANDO CUENTA..." : "CREAR CUENTA"}
            </button>
          </form>

          <p className="registro-login">
            ¿Ya tienes una cuenta?{" "}
            <Link to="/login">Inicia sesión</Link>
          </p>
        </div>

        <aside className="login-welcome-side">
          <h2>¡Únete a ENLÁSATE!</h2>

          <p>
            Crea tu cuenta y descubre las opciones que tenemos para ti.
            Podrás elegir tu plan después de registrarte.
          </p>

          <Link to="/login" className="btn-register">
            Iniciar sesión
          </Link>
        </aside>
      </section>
    </main>
  );
}

export default Registro;