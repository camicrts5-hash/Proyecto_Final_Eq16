package com.enlasate.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;

public record PagoSimuladoRequest(

    @NotNull(message = "Debes seleccionar un plan")
    @Positive(message = "El plan seleccionado no es válido")
    Integer idPlan,

    @NotBlank(message = "Debes seleccionar un método de pago")
    @Pattern(
        regexp = "tarjeta|transferencia|efectivo",
        message = "El método de pago no es válido"
    )
    String metodoPago

) {}