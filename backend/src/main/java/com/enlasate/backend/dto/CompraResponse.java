package com.enlasate.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CompraResponse(
    Integer idCompra,
    Integer idCliente,
    String nombreCliente,
    Integer idEquipo,
    String modeloEquipo,
    String marcaEquipo,
    BigDecimal precioEquipo,
    LocalDate fechaCompra
) {}