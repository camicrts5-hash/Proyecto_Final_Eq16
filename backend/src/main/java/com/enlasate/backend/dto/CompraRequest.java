package com.enlasate.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CompraRequest(

    @NotNull(message = "El cliente es obligatorio")
    @Positive(message = "El ID del cliente debe ser válido")
    Integer idCliente,

    @NotNull(message = "El equipo es obligatorio")
    @Positive(message = "El ID del equipo debe ser válido")
    Integer idEquipo,

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    BigDecimal precioEquipo,

    @NotNull(message = "La fecha de compra es obligatoria")
    LocalDate fechaCompra

) {}