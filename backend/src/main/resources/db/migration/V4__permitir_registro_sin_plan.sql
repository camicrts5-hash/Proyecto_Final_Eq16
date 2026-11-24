-- Permitir que el usuario se registre sin apellido
ALTER TABLE usuario
MODIFY COLUMN apellido VARCHAR(60) NULL;

-- Permitir que el cliente se registre sin seleccionar un plan
ALTER TABLE cliente
MODIFY COLUMN id_plan INT NULL;