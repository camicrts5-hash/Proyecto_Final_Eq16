-- =====================================================================
--  V14 · Periodos de servicio (altas, bajas y reactivaciones)
-- ---------------------------------------------------------------------
--  Permite saber exactamente qué meses tuvo servicio cada cliente,
--  incluso si se dio de baja un tiempo y volvió después.
--  Un periodo abierto (fecha_fin NULL) significa que sigue activo.
-- =====================================================================

CREATE TABLE periodo_servicio (
    id_periodo   INT          NOT NULL AUTO_INCREMENT,
    id_cliente   INT          NOT NULL,
    fecha_inicio DATE         NOT NULL,
    fecha_fin    DATE         NULL,
    motivo       VARCHAR(120) NULL,
    PRIMARY KEY (id_periodo),
    KEY ix_periodo_cliente (id_cliente),
    KEY ix_periodo_fechas (fecha_inicio, fecha_fin),
    CONSTRAINT fk_periodo_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT ck_periodo_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Cada cliente que ya existe arranca un periodo abierto desde su
-- instalación (o su alta si nunca se registró instalación).
-- ---------------------------------------------------------------------
INSERT INTO periodo_servicio (id_cliente, fecha_inicio, motivo)
SELECT c.id_cliente,
       COALESCE(DATE(c.fecha_instalacion), c.fecha_alta),
       'Alta del servicio'
  FROM cliente c
 WHERE c.fecha_alta IS NOT NULL
   AND NOT EXISTS (
        SELECT 1 FROM periodo_servicio p WHERE p.id_cliente = c.id_cliente
   );
