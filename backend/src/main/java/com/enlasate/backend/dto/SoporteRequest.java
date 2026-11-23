package com.enlasate.backend.dto;

import jakarta.validation.constraints.*;

public record SoporteRequest(

        @NotNull(message = "El cliente es obligatorio")
        Integer idCliente,

        @NotNull(message = "El empleado es obligatorio")
        Integer idEmpleado,

        @NotBlank(message = "La descripción de la falla es obligatoria")
        @Size(max = 255, message = "Máximo 255 caracteres")
        String descripcionFalla
) {}