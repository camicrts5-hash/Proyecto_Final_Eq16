-- =====================================================================
--  V15 · Historial de pagos de prueba más realista (2024, 2025 y 2026)
-- ---------------------------------------------------------------------
--  Genera pagos mensuales para los 12 clientes de prueba, deja huecos
--  en algunos para simular morosidad y cierra/abre periodos de servicio
--  para simular bajas y reactivaciones.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Pagos de 2024 y 2025 para los clientes de prueba (id_usuario 5..16)
-- ---------------------------------------------------------------------
INSERT INTO pago_total
    (id_cliente, id_plan, monto, mes_correspondiente, fecha_pago, metodo_pago, estado_pago)
SELECT c.id_cliente,
       c.id_plan,
       pl.precio_mensual,
       CONCAT(a.anio, '-', LPAD(m.mes, 2, '0')),
       LAST_DAY(CONCAT(a.anio, '-', LPAD(m.mes, 2, '0'), '-01')),
       ELT(1 + MOD(c.id_cliente + m.mes, 4), 'Efectivo', 'Tarjeta', 'Transferencia', 'Deposito'),
       'Completado'
  FROM cliente c
  JOIN plan pl ON pl.id_plan = c.id_plan
  JOIN (SELECT 2024 AS anio UNION ALL SELECT 2025) a
  JOIN (
        SELECT 1 AS mes UNION ALL SELECT 2  UNION ALL SELECT 3  UNION ALL
        SELECT 4 UNION ALL SELECT 5  UNION ALL SELECT 6  UNION ALL
        SELECT 7 UNION ALL SELECT 8  UNION ALL SELECT 9  UNION ALL
        SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
       ) m
 WHERE c.id_usuario BETWEEN 5 AND 16
   AND NOT EXISTS (
        SELECT 1 FROM pago_total x
         WHERE x.id_cliente = c.id_cliente
           AND x.mes_correspondiente = CONCAT(a.anio, '-', LPAD(m.mes, 2, '0'))
   );

-- ---------------------------------------------------------------------
-- 2. Huecos de morosidad para que se vean meses en rojo
-- ---------------------------------------------------------------------
DELETE FROM pago_total
 WHERE id_cliente IN (SELECT id_cliente FROM cliente WHERE id_usuario IN (6, 9))
   AND mes_correspondiente IN ('2025-03', '2025-04', '2025-05');

DELETE FROM pago_total
 WHERE id_cliente IN (SELECT id_cliente FROM cliente WHERE id_usuario IN (11, 14))
   AND mes_correspondiente IN ('2024-08', '2024-09');

-- ---------------------------------------------------------------------
-- 3. Baja y reactivación: dos clientes estuvieron sin servicio un tiempo
-- ---------------------------------------------------------------------
-- Cliente 8 (usuario 12): estuvo dado de baja de julio a diciembre de 2025
UPDATE periodo_servicio
   SET fecha_inicio = '2024-01-15',
       fecha_fin    = '2025-07-01',
       motivo       = 'Baja voluntaria'
 WHERE id_cliente = (SELECT id_cliente FROM cliente WHERE id_usuario = 12)
   AND fecha_fin IS NULL;

INSERT INTO periodo_servicio (id_cliente, fecha_inicio, fecha_fin, motivo)
SELECT c.id_cliente, '2026-01-10', NULL, 'Reactivación del servicio'
  FROM cliente c
 WHERE c.id_usuario = 12
   AND NOT EXISTS (
        SELECT 1 FROM periodo_servicio p
         WHERE p.id_cliente = c.id_cliente AND p.fecha_inicio = '2026-01-10'
   );

-- Cliente 10 (usuario 14): sigue de baja desde septiembre de 2025
UPDATE periodo_servicio
   SET fecha_inicio = '2024-02-01',
       fecha_fin    = '2025-09-30',
       motivo       = 'Baja por cambio de domicilio'
 WHERE id_cliente = (SELECT id_cliente FROM cliente WHERE id_usuario = 14)
   AND fecha_fin IS NULL;

-- Esos clientes quedan marcados como inactivos
UPDATE cliente
   SET estado = 'Inactivo'
 WHERE id_usuario IN (12, 14);
