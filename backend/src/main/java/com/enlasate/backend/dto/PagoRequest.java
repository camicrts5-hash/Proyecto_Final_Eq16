package com.enlasate.backend.dto;

import jakarta.validation.constraints.*;

public record PagoRequest(

        @NotNull(message = "El cliente es obligatorio")
        Integer idCliente,

        @NotBlank(message = "El mes es obligatorio")
        @Pattern(regexp = "^[0-9]{4}-(0[1-9]|1[0-2])$", message = "El mes debe tener formato YYYY-MM")
        String mesCorrespondiente,

        @NotBlank(message = "El método de pago es obligatorio")
        @Pattern(regexp = "Efectivo|Transferencia|Tarjeta|Deposito", message = "Método de pago no válido")
        String metodoPago
) {}