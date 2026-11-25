-- =====================================================================
--  V11 · Todo cliente con cuenta debe tener asesor asignado
-- ---------------------------------------------------------------------
--  Los clientes que ya existían se quedaron sin asesor cuando se creó
--  la columna. Aquí se les asigna uno para que el sistema pueda exigir
--  "si no tienes asesor, elige uno".
-- =====================================================================

UPDATE cliente
   SET id_empleado = (SELECT MIN(e.id_empleado) FROM empleado e)
 WHERE id_empleado IS NULL
   AND id_usuario IS NOT NULL
   AND EXISTS (SELECT 1 FROM empleado);
