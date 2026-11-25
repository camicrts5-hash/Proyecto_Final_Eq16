-- =====================================================================
--  V7 · Datos reales de los asesores
-- =====================================================================
--  Antes (datos de prueba):        Ahora (reales):
--    Karen Méndez  9515015001   →   Karen Hernández  9513565719
--    Erandi Ruíz   9515015002   →   Erandi Ruiz      9516520281
--    Javier Pérez  9515015003   →   Karla Martínez   9514931471
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Karen Hernández · Ejecutiva de atención
-- ---------------------------------------------------------------------
UPDATE usuario
   SET nombre   = 'Karen',
       apellido = 'Hernández',
       telefono = '9513565719'
 WHERE email = 'karen@enlasate.mx';

-- ---------------------------------------------------------------------
-- 2. Erandi Ruiz · Técnica instaladora
-- ---------------------------------------------------------------------
UPDATE usuario
   SET nombre   = 'Erandi',
       apellido = 'Ruiz',
       telefono = '9516520281'
 WHERE email = 'erandi@enlasate.mx';

-- ---------------------------------------------------------------------
-- 3. Karla Martínez · Atención a clientes
--    (sustituye al usuario de prueba "Javier Pérez")
-- ---------------------------------------------------------------------
UPDATE usuario
   SET usuario  = 'karla.mtz',
       email    = 'karla@enlasate.mx',
       nombre   = 'Karla',
       apellido = 'Martínez',
       telefono = '9514931471'
 WHERE email = 'soporte@enlasate.mx';

UPDATE empleado e
  JOIN usuario u ON u.id_usuario = e.id_usuario
   SET e.puesto = 'Atención a clientes'
 WHERE u.email = 'karla@enlasate.mx';

-- ---------------------------------------------------------------------
-- 4. Dejar los puestos alineados
-- ---------------------------------------------------------------------
UPDATE empleado e
  JOIN usuario u ON u.id_usuario = e.id_usuario
   SET e.puesto = 'Ejecutiva de atención'
 WHERE u.email = 'karen@enlasate.mx';

UPDATE empleado e
  JOIN usuario u ON u.id_usuario = e.id_usuario
   SET e.puesto = 'Técnica instaladora'
 WHERE u.email = 'erandi@enlasate.mx';
