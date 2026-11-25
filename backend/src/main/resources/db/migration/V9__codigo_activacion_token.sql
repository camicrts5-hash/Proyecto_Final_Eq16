-- =====================================================================
--  V9 · El código de activación ahora es un token firmado (JWT)
--       Por eso la columna debe crecer de 10 a 512 caracteres.
-- =====================================================================

ALTER TABLE cliente
    MODIFY COLUMN codigo_activacion VARCHAR(512) NULL;

-- Los códigos viejos de 4 dígitos ya no sirven con el nuevo flujo:
-- se limpian para que el asesor genere uno nuevo desde el panel.
UPDATE cliente
   SET codigo_activacion = NULL,
       codigo_expira     = NULL
 WHERE estado = 'Pendiente';
