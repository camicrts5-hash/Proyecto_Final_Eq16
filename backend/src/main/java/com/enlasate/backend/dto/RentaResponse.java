package com.enlasate.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RentaResponse(
        Integer idRenta,
        Integer idCliente,
        String nombreCliente,
        Integer idEquipo,
        String modeloEquipo,
        String marcaEquipo,
        BigDecimal rentaMensualEquipo,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {}