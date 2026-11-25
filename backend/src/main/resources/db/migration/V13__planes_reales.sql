-- =====================================================================
--  V13 · Planes reales de ENLÁSATE
-- ---------------------------------------------------------------------
--  Antes (5 planes de prueba):        Ahora (3 planes reales):
--    Básico 10 MB      $300     →      Básico 10 MB       $300
--    Hogar 20 MB       $400     →      Residencial 20 MB  $500
--    Pro 50 MB         $700     →      Comercial 50 MB    $1000
--    Ultra 100 MB      $1100    →      (se elimina)
--    Negocio 200 MB    $1900    →      (se elimina)
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Renombrar y actualizar los tres planes que se quedan
-- ---------------------------------------------------------------------
UPDATE plan
   SET nombre_plan    = 'Básico 10 MB',
       velocidad_mb   = 10,
       precio_mensual = 300.00
 WHERE id_plan = 1;

UPDATE plan
   SET nombre_plan    = 'Residencial 20 MB',
       velocidad_mb   = 20,
       precio_mensual = 500.00
 WHERE id_plan = 2;

UPDATE plan
   SET nombre_plan    = 'Comercial 50 MB',
       velocidad_mb   = 50,
       precio_mensual = 1000.00
 WHERE id_plan = 3;

-- ---------------------------------------------------------------------
-- 2. Los clientes y pagos de los planes eliminados pasan al Comercial
-- ---------------------------------------------------------------------
UPDATE cliente
   SET id_plan = 3
 WHERE id_plan IN (4, 5);

UPDATE pago_total
   SET id_plan = 3
 WHERE id_plan IN (4, 5);

-- ---------------------------------------------------------------------
-- 3. Completar el plan en los pagos que no lo tenían
-- ---------------------------------------------------------------------
UPDATE pago_total p
  JOIN cliente c ON c.id_cliente = p.id_cliente
   SET p.id_plan = c.id_plan
 WHERE p.id_plan IS NULL
   AND c.id_plan IS NOT NULL;

-- ---------------------------------------------------------------------
-- 4. Que cada pago coincida con el precio del plan del cliente
--    (así los datos de prueba quedan consistentes)
-- ---------------------------------------------------------------------
UPDATE pago_total p
  JOIN plan pl ON pl.id_plan = p.id_plan
   SET p.monto = pl.precio_mensual;

-- ---------------------------------------------------------------------
-- 5. Eliminar los planes que ya no se venden
-- ---------------------------------------------------------------------
DELETE FROM plan WHERE id_plan IN (4, 5);
