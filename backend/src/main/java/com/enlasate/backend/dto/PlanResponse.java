package com.enlasate.backend.dto;

import java.math.BigDecimal;

public record PlanResponse(
    Integer idPlan,
    String nombrePlan,
    Integer velocidadMb,
    BigDecimal precioMensual
) {}