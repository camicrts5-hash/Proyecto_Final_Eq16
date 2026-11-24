package com.enlasate.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoResponse(
    Integer idPago,
    Integer idCliente,
    String nombreCliente,
    BigDecimal monto,
    String mesCorrespondiente,
    LocalDate fechaPago,
    String metodoPago,
    String nombrePlan,
    String estadoPago
) {}