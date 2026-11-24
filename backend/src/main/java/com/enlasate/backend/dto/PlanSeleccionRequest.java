package com.enlasate.backend.dto;

import jakarta.validation.constraints.NotNull;

public record PlanSeleccionRequest(

    @NotNull(message = "Debes seleccionar un plan")
    Integer idPlan

) {}