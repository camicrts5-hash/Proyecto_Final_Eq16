package com.enlasate.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RentaRequest(

        @NotNull(message = "El cliente es obligatorio")
        @Positive(message = "El ID del cliente debe ser positivo")
        Integer idCliente,

        @NotNull(message = "El equipo es obligatorio")
        @Positive(message = "El ID del equipo debe ser positivo")
        Integer idEquipo,

        @NotNull(message = "La renta mensual es obligatoria")
        @DecimalMin(value = "0.00", message = "La renta mensual no puede ser negativa")
        BigDecimal rentaMensualEquipo,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        LocalDate fechaFin
) {}