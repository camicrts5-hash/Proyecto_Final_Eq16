package com.enlasate.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoResponse(
        Integer idPago,
        String cliente,
        String mesCorrespondiente,
        BigDecimal monto,
        LocalDate fechaPago,
        String metodoPago
) {}