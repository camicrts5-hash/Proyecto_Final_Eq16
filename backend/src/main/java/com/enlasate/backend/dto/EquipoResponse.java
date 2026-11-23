package com.enlasate.backend.dto;

import java.math.BigDecimal;

public record EquipoResponse(
        Integer idEquipo,
        BigDecimal costoEmp,
        String modelo,
        String marca,
        String estadoEquipo
) {}