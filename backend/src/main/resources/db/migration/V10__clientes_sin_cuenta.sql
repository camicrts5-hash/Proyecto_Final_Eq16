-- =====================================================================
--  V10 · Clientes sin cuenta en la aplicación
-- ---------------------------------------------------------------------
--  Hay clientes que no usan la web (adultos mayores, negocios, etc.).
--  El empleado o el admin los registra igual, pero sin usuario.
--  Sus datos de contacto viven en la propia tabla cliente.
-- =====================================================================

-- 1. El cliente puede existir sin usuario
ALTER TABLE cliente DROP FOREIGN KEY fk_cliente_usuario;

ALTER TABLE cliente
    MODIFY COLUMN id_usuario INT NULL;

ALTER TABLE cliente
    ADD CONSTRAINT fk_cliente_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE ON DELETE CASCADE;

-- 2. Datos de contacto propios del cliente (cuando no tiene cuenta)
ALTER TABLE cliente
    ADD COLUMN nombre_contacto   VARCHAR(120) NULL,
    ADD COLUMN telefono_contacto VARCHAR(10)  NULL,
    ADD COLUMN email_contacto    VARCHAR(100) NULL;

-- 3. Relleno para los clientes que ya existen con cuenta
UPDATE cliente c
  JOIN usuario u ON u.id_usuario = c.id_usuario
   SET c.nombre_contacto   = CONCAT(u.nombre, ' ', COALESCE(u.apellido, '')),
       c.telefono_contacto = u.telefono,
       c.email_contacto    = u.email
 WHERE c.nombre_contacto IS NULL;
