import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

/**
 * Google regresa aquí con ?token=...&rol=...
 * Se guarda la sesión y se manda al panel que corresponde.
 */
function OAuth2Redirect() {
  const [params] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    const token = params.get("token");
    const rol = String(params.get("rol") || "").toUpperCase();

    if (!token) {
      navigate("/login", { replace: true });
      return;
    }

    localStorage.setItem("token", token);
    localStorage.setItem(
      "usuario",
      JSON.stringify({
        idUsuario: Number(params.get("id")),
        nombre: params.get("nombre") || "",
        email: params.get("email") || "",
        rol,
      })
    );

    if (rol === "ADMIN") navigate("/admin", { replace: true });
    else if (rol === "EMPLEADO") navigate("/empleado", { replace: true });
    else navigate("/dashboard", { replace: true });
  }, [params, navigate]);

  return (
    <div style={{ padding: 40, fontFamily: "sans-serif", color: "#4a5568" }}>
      Entrando con Google…
    </div>
  );
}

export default OAuth2Redirect;
