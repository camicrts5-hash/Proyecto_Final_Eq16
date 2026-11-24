ALTER TABLE pago_total
ADD COLUMN id_plan INT NULL,
ADD COLUMN estado_pago VARCHAR(15) NOT NULL DEFAULT 'Completado';

ALTER TABLE pago_total
ADD CONSTRAINT fk_pago_plan
FOREIGN KEY (id_plan)
REFERENCES plan (id_plan)
ON UPDATE CASCADE
ON DELETE RESTRICT;

ALTER TABLE pago_total
ADD CONSTRAINT ck_pago_estado
CHECK (estado_pago IN ('Pendiente', 'Completado', 'Rechazado'));