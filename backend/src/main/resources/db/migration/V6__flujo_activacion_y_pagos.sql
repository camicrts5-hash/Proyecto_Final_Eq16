-- =====================================================================
--  V6 · Flujo de activación del servicio, empleado asignado y cobranza
-- =====================================================================
--  1. El cliente nace en estado "Pendiente" hasta que se le instala
--  2. Código de activación que entrega el empleado
--  3. Empleado asignado al cliente (el cliente lo elige)
--  4. Empleado que registró el pago en efectivo/depósito
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Nuevo estado "Pendiente" en cliente
-- ---------------------------------------------------------------------
ALTER TABLE cliente DROP CHECK ck_cliente_estado;

ALTER TABLE cliente
    MODIFY COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'Pendiente';

ALTER TABLE cliente
    ADD CONSTRAINT ck_cliente_estado
    CHECK (estado IN ('Pendiente', 'Activo', 'Inactivo', 'Moroso'));

-- ---------------------------------------------------------------------
-- 2. Código de activación y fecha de instalación
-- ---------------------------------------------------------------------
ALTER TABLE cliente
    ADD COLUMN codigo_activacion VARCHAR(10) NULL,
    ADD COLUMN fecha_instalacion DATETIME NULL;

-- ---------------------------------------------------------------------
-- 3. Empleado asignado al cliente
-- ---------------------------------------------------------------------
ALTER TABLE cliente
    ADD COLUMN id_empleado INT NULL,
    ADD CONSTRAINT fk_cliente_empleado
        FOREIGN KEY (id_empleado) REFERENCES empleado (id_empleado)
        ON UPDATE CASCADE ON DELETE SET NULL;

-- ---------------------------------------------------------------------
-- 4. Quién registró el pago (solo empleado/admin cobran en efectivo)
-- ---------------------------------------------------------------------
ALTER TABLE pago_total
    ADD COLUMN id_empleado INT NULL,
    ADD CONSTRAINT fk_pago_empleado
        FOREIGN KEY (id_empleado) REFERENCES empleado (id_empleado)
        ON UPDATE CASCADE ON DELETE SET NULL;

-- ---------------------------------------------------------------------
-- 5. Los clientes que ya existían quedan activos e instalados
-- ---------------------------------------------------------------------
UPDATE cliente
   SET estado = 'Activo',
       fecha_instalacion = fecha_alta
 WHERE estado = 'Activo';
