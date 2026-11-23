-- =====================================================================
--  ENLASATE · Programación Web
--  V2 · Datos de prueba (seed)
-- =====================================================================
--  Va en:  src/main/resources/db/migration/
--  Cumple el requisito: 10 a 15 registros por tabla principal.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. ROLES (3 niveles de usuario)
-- ---------------------------------------------------------------------
INSERT INTO rol (id_rol, nombre_rol, descripcion) VALUES
    (1, 'Admin',    'Control total del sistema'),
    (2, 'Empleado', 'Atiende tickets y registra pagos'),
    (3, 'Cliente',  'Solo consulta su estado de cuenta');

-- ---------------------------------------------------------------------
-- 2. USUARIOS (16: 1 admin, 3 empleados, 12 clientes)
--    El hash corresponde a la contraseña Enlasate2026!
-- ---------------------------------------------------------------------
INSERT INTO usuario (id_usuario, usuario, password, email, nombre, apellido, estado, telefono, id_rol) VALUES
    (1,  'admin',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@enlasate.mx',  'Maricarmen', 'Velázquez', 'Activo',   '9515015000', 1),
    (2,  'karen.rh',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'karen@enlasate.mx',  'Karen',      'Méndez',    'Activo',   '9515015001', 2),
    (3,  'erandi.tec', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'erandi@enlasate.mx', 'Erandi',     'Ruíz',      'Activo',   '9515015002', 2),
    (4,  'soporte1',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'soporte@enlasate.mx','Javier',     'Pérez',     'Activo',   '9515015003', 2),
    (5,  'camila.r',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'camila@correo.com',  'Camila',     'Ruíz',      'Activo',   '9511234567', 3),
    (6,  'luis.hdez',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'luis@correo.com',    'Luis',       'Hernández', 'Activo',   '9512345678', 3),
    (7,  'ana.lopez',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ana@correo.com',     'Ana',        'López',     'Activo',   '9513456789', 3),
    (8,  'jorge.mtz',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'jorge@correo.com',   'Jorge',      'Martínez',  'Activo',   '9514567890', 3),
    (9,  'sofia.gm',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'sofia@correo.com',   'Sofía',      'Gómez',     'Activo',   '9515678901', 3),
    (10, 'diego.rm',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'diego@correo.com',   'Diego',      'Ramírez',   'Activo',   '9516789012', 3),
    (11, 'maria.crz',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'maria@correo.com',   'María',      'Cruz',      'Activo',   '9517890123', 3),
    (12, 'pedro.sz',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'pedro@correo.com',   'Pedro',      'Sánchez',   'Activo',   '9518901234', 3),
    (13, 'lucia.fl',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'lucia@correo.com',   'Lucía',      'Flores',    'Activo',   '9519012345', 3),
    (14, 'raul.vg',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'raul@correo.com',    'Raúl',       'Vega',      'Activo',   '9510123456', 3),
    (15, 'elena.ort',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'elena@correo.com',   'Elena',      'Ortiz',     'Activo',   '9511122334', 3),
    (16, 'tomas.rios', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'tomas@correo.com',   'Tomás',      'Ríos',      'Activo',   '9512233445', 3);

-- ---------------------------------------------------------------------
-- 3. EMPLEADOS
-- ---------------------------------------------------------------------
INSERT INTO empleado (id_empleado, id_usuario, puesto) VALUES
    (1, 2, 'Ejecutiva de atención'),
    (2, 3, 'Técnica instaladora'),
    (3, 4, 'Soporte técnico');

-- ---------------------------------------------------------------------
-- 4. PLANES
-- ---------------------------------------------------------------------
INSERT INTO plan (id_plan, nombre_plan, velocidad_mb, precio_mensual) VALUES
    (1, 'Básico 10 MB',  10,  300.00),
    (2, 'Hogar 20 MB',   20,  400.00),
    (3, 'Pro 50 MB',     50,  700.00),
    (4, 'Ultra 100 MB', 100, 1100.00),
    (5, 'Negocio 200 MB',200, 1900.00);

-- ---------------------------------------------------------------------
-- 5. ESTADOS Y MUNICIPIOS
-- ---------------------------------------------------------------------
INSERT INTO estado (id_estado, nombre_estado) VALUES
    (1, 'Oaxaca'),
    (2, 'Puebla');

INSERT INTO municipio (id_municipio, id_estado, nombre_municipio) VALUES
    (1, 1, 'Oaxaca de Juárez'),
    (2, 1, 'San Bartolo Coyotepec'),
    (3, 1, 'Santa Cruz Xoxocotlán'),
    (4, 2, 'Puebla'),
    (5, 2, 'San Andrés Cholula'),
    (6, 1, 'Tlalixtac de Cabrera');

-- ---------------------------------------------------------------------
-- 6. CLIENTES (12)
-- ---------------------------------------------------------------------
INSERT INTO cliente (id_cliente, id_usuario, id_plan, fecha_alta, estado) VALUES
    (1,  5, 2, '2026-03-01', 'Activo'),
    (2,  6, 1, '2026-05-12', 'Activo'),
    (3,  7, 3, '2026-06-20', 'Activo'),
    (4,  8, 1, '2026-08-03', 'Moroso'),
    (5,  9, 2, '2026-09-15', 'Activo'),
    (6, 10, 2, '2026-11-10', 'Activo'),
    (7, 11, 4, '2026-04-02', 'Activo'),
    (8, 12, 1, '2026-07-07', 'Activo'),
    (9, 13, 3, '2026-02-18', 'Activo'),
    (10,14, 2, '2026-10-01', 'Moroso'),
    (11,15, 5, '2026-01-20', 'Activo'),
    (12,16, 1, '2026-11-15', 'Activo');

-- ---------------------------------------------------------------------
-- 7. DOMICILIOS (12)
-- ---------------------------------------------------------------------
INSERT INTO domicilio (id_domicilio, id_cliente, id_municipio, calle, numero, colonia, codigo_postal) VALUES
    (1,  1, 1, 'Av. Juárez',            '204-B', 'Centro',         '68000'),
    (2,  2, 1, 'Calle 20 de Noviembre', '115',   'Reforma',        '68050'),
    (3,  3, 3, 'Av. Universidad',       's/n',   'Xoxocotlán',     '68120'),
    (4,  4, 2, 'Calle Morelos',         '8',     'San Bartolo',    '71256'),
    (5,  5, 4, 'Av. Reforma',           '1902',  'Centro',         '72000'),
    (6,  6, 1, 'Calle Hidalgo',         '45',    'Jalatlaco',      '68080'),
    (7,  7, 5, 'Av. 5 de Mayo',         '301',   'Cholula',        '72760'),
    (8,  8, 1, 'Privada de Xochimilco', '12',    'Xochimilco',     '68040'),
    (9,  9, 6, 'Camino Real',           '77',    'Tlalixtac',      '68230'),
    (10,10, 1, 'Calle Independencia',   '910',   'Centro',         '68000'),
    (11,11, 4, 'Av. Juárez',            '2500',  'La Paz',         '72160'),
    (12,12, 3, 'Calle Framboyanes',     '33',    'Xoxocotlán',     '68120');

-- ---------------------------------------------------------------------
-- 8. EQUIPOS (12)
-- ---------------------------------------------------------------------
INSERT INTO equipo (id_equipo, costo_emp, modelo, marca, estado_equipo) VALUES
    (1,   850.00, 'Archer C20',  'TP-Link', 'Rentado'),
    (2,   850.00, 'Archer C20',  'TP-Link', 'Rentado'),
    (3,  1200.00, 'AX1800',      'Huawei',  'Rentado'),
    (4,  1200.00, 'AX1800',      'Huawei',  'Rentado'),
    (5,   650.00, 'TL-WR840N',   'TP-Link', 'Rentado'),
    (6,  1500.00, 'Deco X20',    'TP-Link', 'Rentado'),
    (7,  1500.00, 'Deco X20',    'TP-Link', 'Rentado'),
    (8,   980.00, 'Archer AX10', 'TP-Link', 'Rentado'),
    (9,  1200.00, 'AX1800',      'Huawei',  'Vendido'),
    (10,  650.00, 'TL-WR840N',   'TP-Link', 'Vendido'),
    (11,  980.00, 'Archer AX10', 'TP-Link', 'Disponible'),
    (12, 1800.00, 'Deco X60',    'TP-Link', 'Disponible');

-- ---------------------------------------------------------------------
-- 9. RENTAS (8)
-- ---------------------------------------------------------------------
INSERT INTO renta_equipo (id_renta, id_cliente, id_equipo, renta_mensual_equipo, fecha_inicio, fecha_fin) VALUES
    (1, 1, 1, 50.00, '2026-03-05', '2027-03-05'),
    (2, 2, 2, 40.00, '2026-05-15', '2027-05-15'),
    (3, 3, 3, 50.00, '2026-06-25', '2027-06-25'),
    (4, 4, 4, 60.00, '2026-08-05', '2027-08-05'),
    (5, 5, 5, 45.00, '2026-09-20', '2027-09-20'),
    (6, 6, 6, 50.00, '2026-11-12', '2027-11-12'),
    (7, 7, 7, 50.00, '2026-04-05', '2027-04-05'),
    (8, 8, 8, 40.00, '2026-07-10', '2027-07-10');

-- ---------------------------------------------------------------------
-- 10. COMPRAS (2)
-- ---------------------------------------------------------------------
INSERT INTO compra_equipo (id_compra, id_cliente, id_equipo, precio_equipo, fecha_compra) VALUES
    (1, 9,  9, 1200.00, '2026-02-20'),
    (2, 10, 10, 850.00, '2026-10-05');

-- ---------------------------------------------------------------------
-- 11. PAGOS (44)
-- ---------------------------------------------------------------------
INSERT INTO pago_total (id_cliente, monto, mes_correspondiente, fecha_pago, metodo_pago) VALUES
    (1, 450.00, '2026-08', '2026-08-05', 'Efectivo'),
    (1, 450.00, '2026-09', '2026-09-04', 'Transferencia'),
    (1, 450.00, '2026-10', '2026-10-06', 'Tarjeta'),
    (1, 450.00, '2026-11', '2026-11-04', 'Efectivo'),

    (2, 340.00, '2026-09', '2026-09-06', 'Efectivo'),
    (2, 340.00, '2026-10', '2026-10-03', 'Efectivo'),
    (2, 340.00, '2026-11', '2026-11-05', 'Transferencia'),

    (3, 750.00, '2026-08', '2026-08-08', 'Transferencia'),
    (3, 750.00, '2026-09', '2026-09-07', 'Transferencia'),
    (3, 750.00, '2026-10', '2026-10-09', 'Tarjeta'),
    (3, 750.00, '2026-11', '2026-11-08', 'Transferencia'),

    (4, 360.00, '2026-10', '2026-10-12', 'Efectivo'),

    (5, 445.00, '2026-10', '2026-10-18', 'Deposito'),
    (5, 445.00, '2026-11', '2026-11-06', 'Efectivo'),

    (7, 1150.00, '2026-06', '2026-06-06', 'Transferencia'),
    (7, 1150.00, '2026-07', '2026-07-05', 'Transferencia'),
    (7, 1150.00, '2026-08', '2026-08-06', 'Tarjeta'),
    (7, 1150.00, '2026-09', '2026-09-05', 'Transferencia'),
    (7, 1150.00, '2026-10', '2026-10-06', 'Transferencia'),
    (7, 1150.00, '2026-11', '2026-11-07', 'Tarjeta'),

    (8, 340.00, '2026-08', '2026-08-09', 'Efectivo'),
    (8, 340.00, '2026-09', '2026-09-08', 'Transferencia'),
    (8, 340.00, '2026-10', '2026-10-08', 'Efectivo'),
    (8, 340.00, '2026-11', '2026-11-09', 'Deposito'),

    (9, 700.00, '2026-03', '2026-03-06', 'Transferencia'),
    (9, 700.00, '2026-04', '2026-04-05', 'Transferencia'),
    (9, 700.00, '2026-05', '2026-05-06', 'Efectivo'),
    (9, 700.00, '2026-06', '2026-06-05', 'Transferencia'),
    (9, 700.00, '2026-07', '2026-07-06', 'Tarjeta'),
    (9, 700.00, '2026-08', '2026-08-05', 'Transferencia'),
    (9, 700.00, '2026-09', '2026-09-06', 'Transferencia'),
    (9, 700.00, '2026-10', '2026-10-05', 'Efectivo'),
    (9, 700.00, '2026-11', '2026-11-06', 'Transferencia'),

    (10, 400.00, '2026-11', '2026-11-10', 'Efectivo'),

    (11, 1900.00, '2026-02', '2026-02-07', 'Transferencia'),
    (11, 1900.00, '2026-03', '2026-03-06', 'Transferencia'),
    (11, 1900.00, '2026-04', '2026-04-07', 'Tarjeta'),
    (11, 1900.00, '2026-05', '2026-05-06', 'Transferencia'),
    (11, 1900.00, '2026-06', '2026-06-07', 'Transferencia'),
    (11, 1900.00, '2026-07', '2026-07-06', 'Efectivo'),
    (11, 1900.00, '2026-08', '2026-08-07', 'Transferencia'),
    (11, 1900.00, '2026-09', '2026-09-06', 'Transferencia'),
    (11, 1900.00, '2026-10', '2026-10-07', 'Tarjeta'),
    (11, 1900.00, '2026-11', '2026-11-06', 'Transferencia');

-- ---------------------------------------------------------------------
-- 12. NOTIFICACIONES (12)
-- ---------------------------------------------------------------------
INSERT INTO notificacion (id_usuario, mensaje, fecha_envio, tipo) VALUES
    (5,  'Su pago de noviembre se registró correctamente.', '2026-11-04 09:15:00', 'Correo'),
    (6,  'Su pago de noviembre se registró correctamente.', '2026-11-05 10:02:00', 'WhatsApp'),
    (8,  'Tiene un adeudo pendiente. Por favor regularícese.', '2026-11-10 08:30:00', 'Correo'),
    (7,  'Su pago de noviembre se registró correctamente.', '2026-11-08 11:20:00', 'Correo'),
    (9,  'Su pago de noviembre se registró correctamente.', '2026-11-06 09:45:00', 'SMS'),
    (11, 'Gracias por su pago puntual.', '2026-11-07 12:00:00', 'Correo'),
    (12, 'Bienvenido a Enlasate. Su instalación es el 18 de noviembre.', '2026-11-15 16:30:00', 'WhatsApp'),
    (13, 'Su pago de noviembre se registró correctamente.', '2026-11-06 09:50:00', 'Correo'),
    (14, 'Tiene un adeudo pendiente. Por favor regularícese.', '2026-11-10 08:35:00', 'SMS'),
    (15, 'Gracias por su pago puntual.', '2026-11-06 10:10:00', 'Correo'),
    (16, 'Bienvenido a Enlasate. Su instalación es el 20 de noviembre.', '2026-11-16 14:00:00', 'Correo'),
    (10, 'Bienvenido a Enlasate. Su instalación es el 14 de noviembre.', '2026-11-11 15:30:00', 'WhatsApp');

-- ---------------------------------------------------------------------
-- 13. TICKETS DE SOPORTE (12)  ·  tabla de la relación N:M
-- ---------------------------------------------------------------------
INSERT INTO soporte_tecnico (id_cliente, id_empleado, descripcion_falla, fecha_reporte, estado_ticket, fecha_cierre) VALUES
    (1,  2, 'Sin señal de internet desde ayer',         '2026-11-05 11:40:00', 'Cerrado',    '2026-11-05 16:00:00'),
    (2,  1, 'El módem se reinicia solo cada hora',      '2026-11-12 09:20:00', 'En proceso', NULL),
    (3,  3, 'El cliente reporta cobro incorrecto',      '2026-11-15 13:05:00', 'Abierto',    NULL),
    (4,  2, 'Velocidad muy baja en la noche',           '2026-11-03 18:10:00', 'Cerrado',    '2026-11-04 10:30:00'),
    (5,  1, 'No puede conectarse por WiFi',             '2026-11-09 08:00:00', 'Cerrado',    '2026-11-09 12:15:00'),
    (6,  3, 'Solicita cambio de contraseña del módem',  '2026-11-14 16:45:00', 'En proceso', NULL),
    (7,  2, 'Corte de fibra en la zona',                '2026-11-06 07:30:00', 'Cerrado',    '2026-11-06 14:00:00'),
    (8,  1, 'Factura con datos incorrectos',            '2026-11-11 10:25:00', 'Abierto',    NULL),
    (9,  3, 'Quiere ampliar su plan',                   '2026-11-13 12:30:00', 'En proceso', NULL),
    (10, 2, 'Módem no enciende',                        '2026-11-07 19:00:00', 'Cerrado',    '2026-11-08 09:45:00'),
    (11, 1, 'Solicita visita técnica',                  '2026-11-16 11:00:00', 'Abierto',    NULL),
    (12, 3, 'Problema con el pago en línea',            '2026-11-17 15:20:00', 'Abierto',    NULL);

-- ---------------------------------------------------------------------
-- 14. VERIFICACIÓN
-- ---------------------------------------------------------------------
SELECT 'rol' AS tabla, COUNT(*) AS filas FROM rol
UNION ALL SELECT 'usuario',         COUNT(*) FROM usuario
UNION ALL SELECT 'empleado',        COUNT(*) FROM empleado
UNION ALL SELECT 'plan',            COUNT(*) FROM plan
UNION ALL SELECT 'cliente',         COUNT(*) FROM cliente
UNION ALL SELECT 'estado',          COUNT(*) FROM estado
UNION ALL SELECT 'municipio',       COUNT(*) FROM municipio
UNION ALL SELECT 'domicilio',       COUNT(*) FROM domicilio
UNION ALL SELECT 'equipo',          COUNT(*) FROM equipo
UNION ALL SELECT 'renta_equipo',    COUNT(*) FROM renta_equipo
UNION ALL SELECT 'compra_equipo',   COUNT(*) FROM compra_equipo
UNION ALL SELECT 'pago_total',      COUNT(*) FROM pago_total
UNION ALL SELECT 'notificacion',    COUNT(*) FROM notificacion
UNION ALL SELECT 'soporte_tecnico', COUNT(*) FROM soporte_tecnico
ORDER BY tabla;
