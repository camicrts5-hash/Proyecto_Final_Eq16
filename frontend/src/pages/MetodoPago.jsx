import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  Wifi,
  CreditCard,
  Building2,
  Banknote,
  CheckCircle2,
  ArrowLeft,
  ShieldCheck,
  LoaderCircle,
} from "lucide-react";
import api from "../services/api";
import "./MetodoPago.css";

function MetodoPago() {
  const navigate = useNavigate();
  const location = useLocation();

  const plan = location.state?.plan;

  const [metodo, setMetodo] = useState("");
  const [procesando, setProcesando] = useState(false);
  const [error, setError] = useState("");
  const [exitoso, setExitoso] = useState(false);

  if (!plan) {
    return (
      <main className="payment-page">
        <div className="payment-card">
          <h2>No se ha seleccionado ningún plan</h2>
          <button onClick={() => navigate("/dashboard")}>
            Volver a los planes
          </button>
        </div>
      </main>
    );
  }

  const confirmarPago = async () => {
  if (!metodo) {
    setError("Selecciona un método de pago para continuar.");
    return;
  }

  try {
    setProcesando(true);
    setError("");

    // Registrar el pago y asociar el plan al cliente
    await api.post("/pagos/mi-pago", {
      idPlan: plan.idPlan,
      metodoPago: metodo,
    });

    setExitoso(true);
  } catch (err) {
    console.error("Error al registrar el pago:", err);

    setError(
      err.response?.data?.message ||
        "No se pudo registrar el pago. Intenta nuevamente."
    );
  } finally {
    setProcesando(false);
  }
};

  if (exitoso) {
    return (
      <main className="payment-page">
        <section className="payment-success">
          <div className="payment-success-icon">
            <CheckCircle2 size={55} />
          </div>

          <span>CONTRATACIÓN COMPLETADA</span>

          <h1>¡Tu conexión está en camino!</h1>

          <p>
            Tu pago simulado se ha procesado correctamente y tu plan
            ya está asociado a tu cuenta.
          </p>

          <div className="payment-success-plan">
            <Wifi size={25} />

            <div>
              <strong>{plan.nombrePlan}</strong>
              <span>{plan.velocidadMb} Mbps</span>
            </div>

            <strong>
              ${Number(plan.precioMensual).toFixed(2)}
            </strong>
          </div>

          <button
            className="payment-confirm-button"
            onClick={() => navigate("/dashboard")}
          >
            IR A MI PANEL
          </button>
        </section>
      </main>
    );
  }

  return (
    <main className="payment-page">
      <header className="payment-header">
        <div className="payment-logo">
          <div>
            <Wifi size={25} />
          </div>
          ENLÁSATE
        </div>

        <span>
          <ShieldCheck size={17} />
          PAGO SIMULADO
        </span>
      </header>

      <section className="payment-container">
        <button
          className="payment-back"
          onClick={() => navigate("/dashboard")}
        >
          <ArrowLeft size={17} />
          Volver a los planes
        </button>

        <div className="payment-heading">
          <span>ÚLTIMO PASO</span>
          <h1>Selecciona tu método de pago</h1>
          <p>
            Elige cómo deseas realizar el pago de tu nuevo plan.
          </p>
        </div>

        <div className="payment-layout">
          <section className="payment-methods">
            <h2>Métodos disponibles</h2>

            <button
              className={`payment-option ${
                metodo === "tarjeta" ? "payment-option-active" : ""
              }`}
              onClick={() => setMetodo("tarjeta")}
            >
              <div className="payment-option-icon">
                <CreditCard size={25} />
              </div>

              <div>
                <strong>Tarjeta bancaria</strong>
                <span>Crédito o débito (simulado)</span>
              </div>

              <div className="payment-radio">
                {metodo === "tarjeta" && <CheckCircle2 size={19} />}
              </div>
            </button>

            <button
              className={`payment-option ${
                metodo === "transferencia" ? "payment-option-active" : ""
              }`}
              onClick={() => setMetodo("transferencia")}
            >
              <div className="payment-option-icon">
                <Building2 size={25} />
              </div>

              <div>
                <strong>Transferencia bancaria</strong>
                <span>Transferencia simulada</span>
              </div>

              <div className="payment-radio">
                {metodo === "transferencia" && (
                  <CheckCircle2 size={19} />
                )}
              </div>
            </button>

            <button
              className={`payment-option ${
                metodo === "efectivo" ? "payment-option-active" : ""
              }`}
              onClick={() => setMetodo("efectivo")}
            >
              <div className="payment-option-icon">
                <Banknote size={25} />
              </div>

              <div>
                <strong>Pago en efectivo</strong>
                <span>Pago en establecimiento (simulado)</span>
              </div>

              <div className="payment-radio">
                {metodo === "efectivo" && <CheckCircle2 size={19} />}
              </div>
            </button>

            <div className="payment-notice">
              <ShieldCheck size={20} />

              <p>
                Esta es una demostración académica. No se realizarán
                cargos reales ni se solicitarán datos bancarios.
              </p>
            </div>
          </section>

          <aside className="payment-summary">
            <span>RESUMEN DE CONTRATACIÓN</span>

            <h2>{plan.nombrePlan}</h2>

            <div className="payment-speed">
              <Wifi size={22} />
              <strong>{plan.velocidadMb} Mbps</strong>
            </div>

            <div className="payment-divider"></div>

            <div className="payment-total">
              <span>Total mensual</span>

              <strong>
                ${Number(plan.precioMensual).toFixed(2)}
              </strong>
            </div>

            <small>Pesos mexicanos (MXN)</small>

            {error && (
              <div className="payment-error">
                {error}
              </div>
            )}

            <button
              className="payment-confirm-button"
              disabled={procesando}
              onClick={confirmarPago}
            >
              {procesando ? (
                <>
                  <LoaderCircle className="payment-spinner" size={18} />
                  PROCESANDO...
                </>
              ) : (
                <>
                  CONFIRMAR CONTRATACIÓN
                  <CheckCircle2 size={18} />
                </>
              )}
            </button>

            <p className="payment-terms">
              Al confirmar, aceptas la contratación del plan seleccionado.
            </p>
          </aside>
        </div>
      </section>
    </main>
  );
}

export default MetodoPago;