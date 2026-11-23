package com.enlasate.backend.dto;

import java.time.LocalDate;
import java.math.BigDecimal;

public record ClienteResponse(
        Integer idCliente,
        String nombre,
        String email,
        String telefono,
        String plan,
        BigDecimal precioPlan,
        LocalDate fechaAlta,
        String estado
) {}