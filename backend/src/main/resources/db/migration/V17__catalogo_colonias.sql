-- ---------------------------------------------------------------------
-- V17 · Catálogo de colonias
--   La colonia deja de ser texto libre dentro de domicilio y pasa a ser
--   un catálogo por municipio (una colonia puede repetirse en municipios
--   distintos, pero no dentro del mismo municipio).
-- ---------------------------------------------------------------------

CREATE TABLE colonia (
    id_colonia     INT         NOT NULL AUTO_INCREMENT,
    id_municipio   INT         NOT NULL,
    nombre_colonia VARCHAR(80) NOT NULL,
    PRIMARY KEY (id_colonia),
    UNIQUE KEY uq_colonia_municipio (id_municipio, nombre_colonia),
    CONSTRAINT fk_colonia_municipio FOREIGN KEY (id_municipio)
        REFERENCES municipio (id_municipio) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 1. Se cargan las colonias que ya estaban capturadas en los domicilios
INSERT IGNORE INTO colonia (id_municipio, nombre_colonia)
SELECT DISTINCT id_municipio, colonia FROM domicilio WHERE id_municipio IS NOT NULL;

-- 2. Colonias base de los municipios de Oaxaca que ya existen
INSERT IGNORE INTO colonia (id_municipio, nombre_colonia) VALUES
    (1, 'Centro'),
    (1, 'Jalatlaco'),
    (1, 'Reforma'),
    (1, 'Xochimilco'),
    (1, 'San Felipe del Agua'),
    (1, 'Santa María del Marquesado'),
    (2, 'San Bartolo'),
    (3, 'Xoxocotlán'),
    (3, 'Santa Cruz'),
    (4, 'Centro'),
    (4, 'La Paz'),
    (5, 'Cholula'),
    (6, 'Tlalixtac');

-- 3. El domicilio guarda también la colonia del catálogo
ALTER TABLE domicilio
    ADD COLUMN id_colonia INT NULL,
    ADD CONSTRAINT fk_domicilio_colonia FOREIGN KEY (id_colonia)
        REFERENCES colonia (id_colonia) ON UPDATE CASCADE ON DELETE RESTRICT;

-- 4. Se enlazan los domicilios existentes con su colonia
UPDATE domicilio d
    JOIN colonia c
        ON c.id_municipio = d.id_municipio
       AND c.nombre_colonia = d.colonia
SET d.id_colonia = c.id_colonia;
