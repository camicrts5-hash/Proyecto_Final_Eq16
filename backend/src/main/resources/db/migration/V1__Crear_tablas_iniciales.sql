-- =====================================================================
--  ENLASATE · Programación Web
--  V1 · Creación de las 15 tablas (MySQL 8)
-- =====================================================================
--  Este archivo va en:  src/main/resources/db/migration/
--  Flyway lo ejecuta solo cuando arranca Spring Boot.
--  NO lleva CREATE DATABASE (Flyway no crea bases) ni USE.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. rol
-- ---------------------------------------------------------------------
CREATE TABLE rol (
    id_rol       INT          NOT NULL AUTO_INCREMENT,
    nombre_rol   VARCHAR(20)  NOT NULL,
    descripcion  VARCHAR(120),
    PRIMARY KEY (id_rol),
    UNIQUE KEY uq_rol_nombre (nombre_rol),
    CONSTRAINT ck_rol_nombre CHECK (nombre_rol IN ('Admin', 'Empleado', 'Cliente'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. usuario
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario  INT          NOT NULL AUTO_INCREMENT,
    usuario     VARCHAR(40)  NOT NULL,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(120) NOT NULL,
    nombre      VARCHAR(60)  NOT NULL,
    apellido    VARCHAR(60)  NOT NULL,
    estado      VARCHAR(10)  NOT NULL DEFAULT 'Activo',
    telefono    VARCHAR(15)  NOT NULL,
    id_rol      INT          NOT NULL,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uq_usuario_usuario (usuario),
    UNIQUE KEY uq_usuario_email (email),
    KEY ix_usuario_rol (id_rol),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol)
        REFERENCES rol (id_rol) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_usuario_estado CHECK (estado IN ('Activo', 'Inactivo', 'Bloqueado'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. empleado
-- ---------------------------------------------------------------------
CREATE TABLE empleado (
    id_empleado INT         NOT NULL AUTO_INCREMENT,
    id_usuario  INT         NOT NULL,
    puesto      VARCHAR(40) NOT NULL,
    PRIMARY KEY (id_empleado),
    UNIQUE KEY uq_empleado_usuario (id_usuario),
    CONSTRAINT fk_empleado_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4. plan
-- ---------------------------------------------------------------------
CREATE TABLE plan (
    id_plan        INT           NOT NULL AUTO_INCREMENT,
    nombre_plan    VARCHAR(50)   NOT NULL,
    velocidad_mb   INT           NOT NULL,
    precio_mensual DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id_plan),
    UNIQUE KEY uq_plan_nombre (nombre_plan),
    CONSTRAINT ck_plan_velocidad CHECK (velocidad_mb > 0),
    CONSTRAINT ck_plan_precio CHECK (precio_mensual > 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5. cliente
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
    id_cliente INT         NOT NULL AUTO_INCREMENT,
    id_usuario INT         NOT NULL,
    id_plan    INT         NOT NULL,
    fecha_alta DATE        NOT NULL,
    estado     VARCHAR(10) NOT NULL DEFAULT 'Activo',
    PRIMARY KEY (id_cliente),
    UNIQUE KEY uq_cliente_usuario (id_usuario),
    KEY ix_cliente_plan (id_plan),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_cliente_plan FOREIGN KEY (id_plan)
        REFERENCES plan (id_plan) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_cliente_estado CHECK (estado IN ('Activo', 'Inactivo', 'Moroso'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 6. estado
-- ---------------------------------------------------------------------
CREATE TABLE estado (
    id_estado     INT         NOT NULL AUTO_INCREMENT,
    nombre_estado VARCHAR(60) NOT NULL,
    PRIMARY KEY (id_estado),
    UNIQUE KEY uq_estado_nombre (nombre_estado)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 7. municipio
-- ---------------------------------------------------------------------
CREATE TABLE municipio (
    id_municipio     INT         NOT NULL AUTO_INCREMENT,
    id_estado        INT         NOT NULL,
    nombre_municipio VARCHAR(80) NOT NULL,
    PRIMARY KEY (id_municipio),
    UNIQUE KEY uq_municipio_nombre (id_estado, nombre_municipio),
    CONSTRAINT fk_municipio_estado FOREIGN KEY (id_estado)
        REFERENCES estado (id_estado) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 8. domicilio
-- ---------------------------------------------------------------------
CREATE TABLE domicilio (
    id_domicilio  INT          NOT NULL AUTO_INCREMENT,
    id_cliente    INT          NOT NULL,
    id_municipio  INT          NOT NULL,
    calle         VARCHAR(120) NOT NULL,
    numero        VARCHAR(10)  NOT NULL,
    colonia       VARCHAR(80)  NOT NULL,
    codigo_postal CHAR(5)      NOT NULL,
    PRIMARY KEY (id_domicilio),
    KEY ix_domicilio_cliente (id_cliente),
    KEY ix_domicilio_municipio (id_municipio),
    CONSTRAINT fk_domicilio_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_domicilio_municipio FOREIGN KEY (id_municipio)
        REFERENCES municipio (id_municipio) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_domicilio_cp CHECK (codigo_postal REGEXP '^[0-9]{5}$')
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 9. equipo
-- ---------------------------------------------------------------------
CREATE TABLE equipo (
    id_equipo     INT           NOT NULL AUTO_INCREMENT,
    costo_emp     DECIMAL(10,2) NOT NULL,
    modelo        VARCHAR(40)   NOT NULL,
    marca         VARCHAR(40)   NOT NULL,
    estado_equipo VARCHAR(12)   NOT NULL DEFAULT 'Disponible',
    PRIMARY KEY (id_equipo),
    CONSTRAINT ck_equipo_costo CHECK (costo_emp >= 0),
    CONSTRAINT ck_equipo_estado CHECK (estado_equipo IN ('Disponible', 'Rentado', 'Vendido', 'Mantenimiento'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 10. renta_equipo
-- ---------------------------------------------------------------------
CREATE TABLE renta_equipo (
    id_renta             INT           NOT NULL AUTO_INCREMENT,
    id_cliente           INT           NOT NULL,
    id_equipo            INT           NOT NULL,
    renta_mensual_equipo DECIMAL(10,2) NOT NULL,
    fecha_inicio         DATE          NOT NULL,
    fecha_fin            DATE          NULL,
    PRIMARY KEY (id_renta),
    UNIQUE KEY uq_renta_cliente (id_cliente),
    KEY ix_renta_equipo (id_equipo),
    CONSTRAINT fk_renta_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_renta_equipo FOREIGN KEY (id_equipo)
        REFERENCES equipo (id_equipo) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_renta_precio CHECK (renta_mensual_equipo >= 0),
    CONSTRAINT ck_renta_fechas CHECK (fecha_fin IS NULL OR fecha_fin > fecha_inicio)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 11. compra_equipo
-- ---------------------------------------------------------------------
CREATE TABLE compra_equipo (
    id_compra     INT           NOT NULL AUTO_INCREMENT,
    id_cliente    INT           NOT NULL,
    id_equipo     INT           NOT NULL,
    precio_equipo DECIMAL(10,2) NOT NULL,
    fecha_compra  DATE          NOT NULL,
    PRIMARY KEY (id_compra),
    UNIQUE KEY uq_compra_cliente (id_cliente),
    KEY ix_compra_equipo (id_equipo),
    CONSTRAINT fk_compra_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_compra_equipo FOREIGN KEY (id_equipo)
        REFERENCES equipo (id_equipo) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_compra_precio CHECK (precio_equipo > 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 12. pago_total
-- ---------------------------------------------------------------------
CREATE TABLE pago_total (
    id_pago             INT           NOT NULL AUTO_INCREMENT,
    id_cliente          INT           NOT NULL,
    monto               DECIMAL(10,2) NOT NULL,
    mes_correspondiente CHAR(7)       NOT NULL,
    fecha_pago          DATE          NOT NULL,
    metodo_pago         VARCHAR(15)   NOT NULL,
    PRIMARY KEY (id_pago),
    UNIQUE KEY uq_pago_cliente_mes (id_cliente, mes_correspondiente),
    KEY ix_pago_mes (mes_correspondiente),
    CONSTRAINT fk_pago_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_pago_monto CHECK (monto > 0),
    CONSTRAINT ck_pago_metodo CHECK (metodo_pago IN ('Efectivo', 'Transferencia', 'Tarjeta', 'Deposito')),
    CONSTRAINT ck_pago_mes CHECK (mes_correspondiente REGEXP '^[0-9]{4}-(0[1-9]|1[0-2])$')
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 13. notificacion
-- ---------------------------------------------------------------------
CREATE TABLE notificacion (
    id_notificacion INT          NOT NULL AUTO_INCREMENT,
    id_usuario      INT          NOT NULL,
    mensaje         VARCHAR(255) NOT NULL,
    fecha_envio     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo            VARCHAR(10)  NOT NULL,
    PRIMARY KEY (id_notificacion),
    KEY ix_notificacion_usuario (id_usuario),
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT ck_notificacion_tipo CHECK (tipo IN ('Correo', 'WhatsApp', 'SMS'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 14. soporte_tecnico  (la relación N:M entre cliente y empleado)
-- ---------------------------------------------------------------------
CREATE TABLE soporte_tecnico (
    id_ticket         INT          NOT NULL AUTO_INCREMENT,
    id_cliente        INT          NOT NULL,
    id_empleado       INT          NOT NULL,
    descripcion_falla VARCHAR(255) NOT NULL,
    fecha_reporte     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_ticket     VARCHAR(12)  NOT NULL DEFAULT 'Abierto',
    fecha_cierre      DATETIME     NULL,
    PRIMARY KEY (id_ticket),
    KEY ix_ticket_cliente (id_cliente),
    KEY ix_ticket_empleado (id_empleado),
    CONSTRAINT fk_ticket_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_ticket_empleado FOREIGN KEY (id_empleado)
        REFERENCES empleado (id_empleado) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_ticket_estado CHECK (estado_ticket IN ('Abierto', 'En proceso', 'Cerrado'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 15. auditoria_pago  (bitácora de cambios en los pagos)
-- ---------------------------------------------------------------------
CREATE TABLE auditoria_pago (
    id_auditoria   INT           NOT NULL AUTO_INCREMENT,
    id_pago        INT           NULL,
    accion         VARCHAR(10)   NOT NULL,
    monto_anterior DECIMAL(10,2) NULL,
    monto_nuevo    DECIMAL(10,2) NULL,
    usuario_bd     VARCHAR(50)   NOT NULL,
    fecha_evento   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_auditoria),
    CONSTRAINT ck_auditoria_accion CHECK (accion IN ('INSERT', 'UPDATE', 'DELETE'))
) ENGINE=InnoDB;
