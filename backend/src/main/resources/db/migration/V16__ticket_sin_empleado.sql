-- =====================================================================
--  V16 · Permite dar de baja a un empleado que tiene tickets
-- ---------------------------------------------------------------------
--  soporte_tecnico.id_empleado era NOT NULL con ON DELETE RESTRICT,
--  asi que borrar un empleado con tickets fallaba con error 409.
--  Ahora el ticket queda sin asignar en vez de bloquear la baja.
-- =====================================================================

ALTER TABLE soporte_tecnico DROP FOREIGN KEY fk_ticket_empleado;

ALTER TABLE soporte_tecnico
    MODIFY COLUMN id_empleado INT NULL;

ALTER TABLE soporte_tecnico
    ADD CONSTRAINT fk_ticket_empleado
        FOREIGN KEY (id_empleado) REFERENCES empleado (id_empleado)
        ON UPDATE CASCADE ON DELETE SET NULL;
