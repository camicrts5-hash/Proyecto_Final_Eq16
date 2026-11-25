-- =====================================================================
--  V8 · Vigencia de 24 horas para el código de activación
-- =====================================================================

ALTER TABLE cliente
    ADD COLUMN codigo_expira DATETIME NULL;

-- Los códigos ya generados vencen 24 h después de este momento
UPDATE cliente
   SET codigo_expira = DATE_ADD(NOW(), INTERVAL 24 HOUR)
 WHERE estado = 'Pendiente'
   AND codigo_activacion IS NOT NULL
   AND codigo_expira IS NULL;
