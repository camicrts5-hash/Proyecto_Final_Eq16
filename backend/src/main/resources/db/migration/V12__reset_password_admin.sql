-- =====================================================================
--  V12 · Restablecer la contraseña del administrador
-- ---------------------------------------------------------------------
--  La contraseña de admin@enlasate.mx se cambió desde la aplicación y
--  la cuenta quedó inaccesible para la demostración.
--  Se restablece a: Enlasate2026!
-- =====================================================================

UPDATE usuario
   SET password = '$2a$10$DXxkxtQ4PX4QpyqEgUPpc.YtWfHQaucLJYJgqNXToBjHsW.XEgbIG'
 WHERE email = 'admin@enlasate.mx';
