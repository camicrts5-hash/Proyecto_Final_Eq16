package com.enlasate.backend.dto;

import jakarta.validation.constraints.NotNull;

public record EmpleadoSeleccionRequest(
        @NotNull(message = "Debes elegir un empleado")
        Integer idEmpleado
) {}
