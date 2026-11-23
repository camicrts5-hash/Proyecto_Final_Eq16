package com.enlasate.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record EquipoRequest(

        @NotNull(message = "El costo es obligatorio")
        @DecimalMin(value = "0.00", message = "El costo no puede ser negativo")
        BigDecimal costoEmp,

        @NotBlank(message = "El modelo es obligatorio")
        String modelo,

        @NotBlank(message = "La marca es obligatoria")
        String marca,

        @NotBlank(message = "El estado es obligatorio")
        @Pattern(regexp = "Disponible|Rentado|Vendido|Mantenimiento", message = "Estado no válido")
        String estadoEquipo
) {}