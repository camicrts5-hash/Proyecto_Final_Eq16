package com.enlasate.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PlanRequest(

        @NotBlank(message = "El nombre del plan es obligatorio")
        @Size(max = 50, message = "Máximo 50 caracteres")
        String nombrePlan,

        @NotNull(message = "La velocidad es obligatoria")
        @Min(value = 1, message = "La velocidad debe ser mayor a 0")
        Integer velocidadMb,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        BigDecimal precioMensual
) {}