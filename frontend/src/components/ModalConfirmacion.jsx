export default function ModalConfirmacion({
  abierto,
  titulo,
  mensaje,
  textoConfirmar = 'Sí, eliminar',
  cargando = false,
  onConfirmar,
  onCancelar,
}) {
  if (!abierto) return null;

  return (
    <div className="modal-fondo" onClick={onCancelar}>
      <div className="modal-caja" onClick={(e) => e.stopPropagation()}>
        <h3 className="modal-titulo">{titulo}</h3>
        <p className="modal-mensaje">{mensaje}</p>

        <div className="modal-acciones">
          <button
            className="modal-btn secundario"
            onClick={onCancelar}
            disabled={cargando}
          >
            Cancelar
          </button>
          <button
            className="modal-btn peligro"
            onClick={onConfirmar}
            disabled={cargando}
          >
            {cargando ? 'Eliminando...' : textoConfirmar}
          </button>
        </div>
      </div>
    </div>
  );
}